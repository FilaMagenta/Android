package com.arnyminerz.filamagenta.database.remote

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.arnyminerz.filamagenta.BuildConfig
import com.arnyminerz.filamagenta.auth.AccountSingleton
import com.arnyminerz.filamagenta.data.event.TableData
import com.arnyminerz.filamagenta.database.local.AppDatabase
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.database.local.entity.PersonData
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData
import com.arnyminerz.filamagenta.utils.serialize
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteInterface private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: RemoteInterface? = null

        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: RemoteInterface(context).also {
                INSTANCE = it
            }
        }
    }

    private val singleton = VolleySingleton.getInstance(context)

    private val accountSingleton = AccountSingleton.getInstance(context)

    private val database = AppDatabase.getInstance(context)

    /**
     * Makes a GET request to the given URL, with the provided headers. Converts the answer to a
     * JSON object.
     * @author Arnau Mora
     * @since 20221025
     * @param url The URL to make the request to.
     * @param headers The headers to use to make the request.
     * @param timeout The maximum amount of time to wait until a response is received. (millis)
     * @return The response parsed into a [JSONObject].
     * @throws JSONException If the response could not be parsed into JSON.
     * @throws IOException If there's an IO exception while making the request.
     * @throws TimeoutCancellationException If the query was timed out.
     * @see tokenHeader
     */
    @WorkerThread
    @Throws(JSONException::class, IOException::class, TimeoutCancellationException::class)
    private suspend fun query(
        url: String,
        headers: Map<String, String>? = null,
        timeout: Long = 10000
    ) = withTimeout(timeout) {
        suspendCancellableCoroutine { cont ->
            Timber.d("Making GET request to: $url. Headers: $headers")
            val request = object : StringRequest(
                Method.GET,
                url,
                { json -> cont.resume(JSONObject(json)) },
                { cont.resumeWithException(it) },
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    if (headers == null)
                        return super.getHeaders()
                    return headers.toMutableMap()
                }
            }
            singleton.addToRequestQueue(request)
        }
    }

    /**
     * Makes a POST request to the given url.
     * @author Arnau Mora
     * @since 20221109
     */
    private suspend fun post(
        url: String,
        body: JSONObject? = null,
        headers: Map<String, String>? = null,
        timeout: Long = 10000,
    ) = withTimeout(timeout) {
        suspendCancellableCoroutine { cont ->
            Timber.d("Making POST request to: $url. Headers: $headers. Body: $body")
            val request = object : StringRequest(
                Method.POST,
                url,
                { json -> cont.resume(JSONObject(json)) },
                { cont.resumeWithException(it) },
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    if (headers == null)
                        return super.getHeaders()
                    return headers.toMutableMap()
                }

                override fun getBodyContentType(): String =
                    if (body == null)
                        super.getBodyContentType()
                    else
                        "application/json; charset=utf-8"

                override fun getBody(): ByteArray? = try {
                    body?.toString()?.toByteArray()
                } catch (e: UnsupportedEncodingException) {
                    VolleyLog.wtf(
                        "Unsupported Encoding while trying to get the bytes of %s using %s",
                        body,
                        "utf-8"
                    )
                    null
                }
            }
            val uuid = singleton.addToRequestQueue(request)
            cont.invokeOnCancellation { singleton.cancel(uuid) }
        }
    }

    /**
     * Checks that a given [JSONObject] contains a `success` field, and it's `true`.
     * @author Arnau Mora
     * @since 20221021
     * @param json The object to check for.
     * @throws IllegalStateException If the conditions do not match.
     */
    @Throws(IllegalStateException::class)
    private fun checkSuccessful(json: JSONObject) =
        check(json.has("success") && json.getBoolean("success"))

    /**
     * Checks that a given [JSONObject] contains a `data` field.
     * @author Arnau Mora
     * @since 20221021
     * @param json The object to check for.
     * @throws IllegalStateException If the conditions do not match.
     */
    @Throws(IllegalStateException::class)
    private fun checkData(json: JSONObject) = check(json.has("data"))

    /**
     * Builds an url for calling an endpoint of V1.
     * @author Arnau Mora
     * @since 20221021
     * @param path The endpoint to use.
     * @param queryParams If not null, some parameters to add at the end of the URL as query params.
     * @return The correctly formatted url.
     */
    @Suppress("KotlinConstantConditions")
    private fun buildV1Url(path: String, queryParams: Map<String, String>? = null) =
        URL(
            BuildConfig.REST_PROTO.takeIf { it != "null" } ?: "https",
            BuildConfig.REST_BASE,
            BuildConfig.REST_PORT.takeIf { it != "null" }?.toInt() ?: 80,
            "/v1$path" + (queryParams?.let {
                "?" + queryParams.toList().joinToString("&") { (k, v) ->
                    val key = URLEncoder.encode(k, Charsets.UTF_8.name())
                    val value = URLEncoder.encode(v, Charsets.UTF_8.name())
                    "$key=$value"
                }
            } ?: "")
        ).toString()

    /**
     * Generates a map with the given token as the API-Key header.
     * @author Arnau Mora
     * @since 20221021
     * @param token The token to use for the header.
     * @param extraHeaders Extra headers that you might want to add.
     * @return A [Map] with the [token] and [extraHeaders].
     */
    private fun tokenHeader(
        token: String?,
        vararg extraHeaders: Pair<String, String>,
    ): Map<String, String> = mutableMapOf(*extraHeaders).apply { token?.let { set("API-Key", it) } }

    @WorkerThread
    @Throws(IllegalStateException::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun logIn(nif: String, password: String): String {
        val url = buildV1Url("/user/auth")
        val response = post(url, JSONObject(mapOf("nif" to nif, "password" to password)))
        checkSuccessful(response)
        checkData(response)

        return response.getJSONObject("data").getString("token")
    }

    /**
     * Fetches the logged in user's data.
     * @author Arnau Mora
     * @since 20221110
     * @param token The authentication token of the user.
     * @throws JSONException If an error is thrown while parsing the response.
     * @return A [PersonData] instance with all the loaded data.
     */
    @WorkerThread
    @Throws(IllegalStateException::class, JSONException::class)
    suspend fun getPersonData(token: String): PersonData {
        val response = query(
            buildV1Url("/user/data"),
            tokenHeader(token),
        )
        checkSuccessful(response)
        checkData(response)

        val data = response.getJSONObject("data")
        Timber.i("data: %s", data.toString())
        val personData = PersonData.fromRest(data)

        // Store into room
        val dao = database.peopleDao()
        dao.getDataById(personData.id)?.run {
            // If already exists, update
            dao.update(personData)
        } ?: run {
            // If not exists, add
            dao.add(personData)
        }

        return personData
    }

    /**
     * Gets the user data of [userId].
     * @author Arnau Mora
     * @since 20221025
     * @param token The token to use for authorisation.
     * @param userId The id of the user to fetch.
     * @return Returns the data of the given user.
     * @throws ClassNotFoundException If there are no logged in accounts.
     * @throws JSONException If the given response could not be parsed.
     */
    @Throws(ClassNotFoundException::class, JSONException::class)
    suspend fun getAccountData(token: String, userId: Long): ShortPersonData {
        val response = query(
            buildV1Url("/user/data", mapOf("user_id" to userId.toString())),
            tokenHeader(token),
        )
        checkSuccessful(response)
        checkData(response)

        val responseData = response.getJSONObject("data")
        val personData = if (responseData.has("vCard"))
            PersonData.fromRest(responseData).short
        else
            responseData.serialize(ShortPersonData)

        database.peopleDao().apply {
            if (getById(personData.id) != null)
                update(personData)
            else
                add(personData)
        }

        return personData
    }

    @WorkerThread
    @Throws(IllegalStateException::class)
    suspend fun getEvents(token: String): List<EventEntity> {
        val response = query(
            buildV1Url("/events/list"),
            tokenHeader(token),
        )
        checkSuccessful(response)
        checkData(response)

        return response.getJSONArray("data").serialize(EventEntity.Companion)
    }

    /**
     * Creates a new table, joins an existing one, or confirms assistance to an event.
     * @author Arnau Mora
     * @since 20221110
     * @param token The token of the authorised user.
     * @param event The event to join.
     * @param table The table to join, or null if it's desired to create a new one.
     * @param assists If the user will be attending to the event or not. Can be null if eat event.
     * @throws VolleyError If the action could not be performed successfully.
     */
    @WorkerThread
    @Throws(VolleyError::class)
    suspend fun joinEvent(token: String, event: EventEntity, table: TableData?, assists: Boolean?) {
        val response = post(
            buildV1Url("/events/${event.id}/join"),
            JSONObject().apply {
                table?.id?.let { put("table_id", it) }
                assists?.let { put("assists", it) }
            },
            tokenHeader(token),
        )
        checkSuccessful(response)
    }
}
