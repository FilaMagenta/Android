package com.arnyminerz.filamagenta.database.remote

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.volley.toolbox.StringRequest
import com.arnyminerz.filamagenta.BuildConfig
import com.arnyminerz.filamagenta.auth.AccountSingleton
import com.arnyminerz.filamagenta.data.account.AccountData
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData
import com.arnyminerz.filamagenta.utils.serialize
import com.arnyminerz.filamagenta.utils.throwUnless
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

    /**
     * Makes a GET request to the given URL, with the provided headers. Converts the answer to a
     * JSON object.
     * @author Arnau Mora
     * @since 20221025
     * @param url The URL to make the request to.
     * @param headers The headers to use to make the request.
     * @return The response parsed into a [JSONObject].
     * @throws JSONException If the response could not be parsed into JSON.
     * @throws IOException If there's an IO exception while making the request.
     * @see tokenHeader
     */
    @WorkerThread
    @Throws(JSONException::class, IOException::class)
    private suspend fun query(url: String, headers: Map<String, String>? = null) =
        suspendCoroutine { cont ->
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
    private fun buildV1Url(path: String, queryParams: Map<String, String>? = null) =
        BuildConfig.REST_BASE + "/v1" + path + (queryParams?.let {
            "?" + queryParams.toList().joinToString { (k, v) ->
                val key = URLEncoder.encode(k, Charsets.UTF_8.name())
                val value = URLEncoder.encode(v, Charsets.UTF_8.name())
                "$key=$value"
            }
        } ?: "")

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
    suspend fun logIn(dni: String, password: String): String {
        val url = buildV1Url("/user/auth", mapOf("dni" to dni, "password" to password))
        val response = query(url)
        checkSuccessful(response)
        checkData(response)

        return response.getJSONObject("data").getString("auth-token")
    }

    /**
     * Fetches the logged in user's data.
     * @author Arnau Mora
     * @since 20221021
     * @param token The authentication token of the user.
     * @return An [AccountData] instance with all the loaded data.
     */
    @WorkerThread
    @Throws(IllegalStateException::class)
    suspend fun getAccountData(token: String): AccountData {
        val response = query(
            buildV1Url("/user/data"),
            tokenHeader(token),
        )
        checkSuccessful(response)
        checkData(response)

        return AccountData.fromJson(response.getJSONObject("data"))
    }

    /**
     * Fetches the logged in user's data.
     * @author Arnau Mora
     * @since 20221025
     * @param index The index of the account to select.
     * @return An [AccountData] instance with all the loaded data.
     * @throws ClassNotFoundException If there are no logged in accounts.
     */
    @Throws(ClassNotFoundException::class)
    @Suppress("ThrowableNotThrown")
    suspend fun getAccountData(index: Int): AccountData {
        val account = accountSingleton.getAccounts()
            .throwUnless(
                { it.isEmpty() },
                ClassNotFoundException("There are no logged in accounts.")
            )[index]
        val token = accountSingleton.getToken(account)
        return getAccountData(token)
    }

    /**
     * Gets the user data of [userId].
     * @author Arnau Mora
     * @since 20221025
     * @param userId The id of the user to fetch.
     * @param accountIndex The index of the account to use. Taken from [AccountSingleton.getAccounts].
     * @return Returns the data of the given user.
     * @throws ClassNotFoundException If there are no logged in accounts.
     * @throws JSONException If the given response could not be parsed.
     */
    @Throws(ClassNotFoundException::class, JSONException::class)
    suspend fun getAccountData(userId: Long, accountIndex: Int): ShortPersonData {
        val token = accountSingleton.getAccounts()
            .throwUnless(
                { it.isEmpty() },
                ClassNotFoundException("There are no logged in accounts.")
            )[accountIndex]
            .let { accountSingleton.getToken(it) }
        val response = query(
            buildV1Url("/v1/user/data", mapOf("user_id" to userId.toString())),
            tokenHeader(token),
        )
        checkSuccessful(response)
        checkData(response)

        return response.getJSONObject("data").serialize(ShortPersonData.Companion)
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
}