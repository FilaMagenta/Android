package com.arnyminerz.filamagenta.data.account

import com.arnyminerz.filamagenta.utils.getDate
import com.arnyminerz.filamagenta.utils.putDate
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONObject
import java.util.Date

data class TrebuchetData(
    val shoots: Boolean,
    val obtained: Date,
    val expires: Date,
) : JsonSerializable() {
    companion object : JsonSerializer<TrebuchetData> {
        override fun fromJson(json: JSONObject): TrebuchetData = TrebuchetData(
            json.getBoolean("shoots"),
            json.getDate("obtained"),
            json.getDate("expires"),
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("shoots", shoots)
        putDate("obtained", obtained)
        putDate("expires", expires)
    }
}
