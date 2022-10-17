package com.arnyminerz.filamagenta.utils

import android.os.Bundle
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val JsonDateFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@Throws(JSONException::class)
fun JSONObject.putDate(key: String, date: Date?) {
    if (date == null)
        return
    val formatted = JsonDateFormat.format(date)
    put(key, formatted)
}

@Throws(JSONException::class, ParseException::class)
fun JSONObject.getDate(key: String): Date {
    val date = getString(key)
    return JsonDateFormat.parse(date)!!
}

fun JSONObject.getStringOrNull(key: String): String? =
    if (has(key))
        getString(key)
    else null

fun JSONObject.getDateOrNull(key: String): Date? =
    if (has(key))
        getDate(key)
    else null

fun JSONObject.getIntOrNull(key: String): Int? =
    if (has(key))
        getInt(key)
    else null

@Throws(JSONException::class)
fun JSONObject.putJson(key: String, obj: JsonSerializable) = put(key, obj.toJson())

@Throws(JSONException::class)
fun Bundle.putJson(key: String, obj: JsonSerializable) = putString(key, obj.toJson().toString())

val String.json: JSONObject
    @Throws(JSONException::class)
    get() = JSONObject(this)

/**
 * Gets all the entries of the object as a map.
 * @author Arnau mora
 * @since 20221014
 */
val JSONObject.entries: Map<String, Any>
    get() = keys().asSequence().associateWith { get(it) }

/**
 * Converts the JSON Array into a list of strings by running [JSONArray.getString] on each index.
 * @author Arnau Mora
 * @since 20221014
 */
val JSONArray.asStringList: List<String>
    get() = (0 until length()).map { getString(it) }

/**
 * Converts the JSON Array into a list of [Long] by running [JSONArray.getLong] on each index.
 * @author Arnau Mora
 * @since 20221015
 */
val JSONArray.asLongList: List<Long>
    get() = (0 until length()).map { getLong(it) }
