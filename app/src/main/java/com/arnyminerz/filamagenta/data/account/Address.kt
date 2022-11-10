package com.arnyminerz.filamagenta.data.account

import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONObject

data class Address(
    val type: String,
    val address: String,
) : JsonSerializable() {
    companion object : JsonSerializer<Address> {
        override fun fromJson(json: JSONObject): Address = Address(
            json.getString("type"),
            json.getString("address"),
        )
    }

    constructor(type: String, address: List<String>) : this(
        type,
        address.filter { it.isNotBlank() }.joinToString(),
    )

    override fun toJson(): JSONObject = JSONObject().apply {
        put("type", type)
        put("address", address)
    }
}
