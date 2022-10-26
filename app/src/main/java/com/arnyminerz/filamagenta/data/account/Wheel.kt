package com.arnyminerz.filamagenta.data.account

import com.arnyminerz.filamagenta.utils.getLongOrNull
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONObject

data class Wheel(
    val locked: Boolean,
    val number: Long?,
) : JsonSerializable() {
    companion object : JsonSerializer<Wheel> {
        override fun fromJson(json: JSONObject): Wheel = Wheel(
            json.getBoolean("locked"),
            json.getLongOrNull("number"),
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("locked", locked)
        put("number", number)
    }
}
