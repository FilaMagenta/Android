package com.arnyminerz.filamagenta.utils.serialize

import org.json.JSONObject

abstract class JsonSerializable {
    abstract fun toJson(): JSONObject

    override fun toString(): String = toJson().toString()

    fun toString(indentSpaces: Int): String = toJson().toString(indentSpaces)
}
