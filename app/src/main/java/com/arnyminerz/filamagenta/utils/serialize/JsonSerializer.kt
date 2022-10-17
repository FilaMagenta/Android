package com.arnyminerz.filamagenta.utils.serialize

import org.json.JSONObject

interface JsonSerializer<R> {
    fun fromJson(json: JSONObject): R
}
