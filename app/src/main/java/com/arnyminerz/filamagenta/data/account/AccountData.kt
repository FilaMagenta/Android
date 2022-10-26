package com.arnyminerz.filamagenta.data.account

import com.arnyminerz.filamagenta.utils.asStringList
import com.arnyminerz.filamagenta.utils.getDate
import com.arnyminerz.filamagenta.utils.getIntOrNull
import com.arnyminerz.filamagenta.utils.getJSONObjectOrNull
import com.arnyminerz.filamagenta.utils.getStringOrNull
import com.arnyminerz.filamagenta.utils.putDate
import com.arnyminerz.filamagenta.utils.serialize
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

data class AccountData(
    val id: Long,
    val name: String,
    val familyName: String,
    val address: String,
    val postalCode: Int,
    val dni: String,
    val born: Date,
    val registrationDate: Date,
    val phone: String?,
    val workPhone: String?,
    val mobilePhone: String?,
    val email: String,
    val profileImage: String?,
    val whiteWheel: Wheel?,
    val blackWheel: Wheel?,
    val age: Int?,
    val trebuchetData: TrebuchetData?,
    val type: FesterType,
    val paymentMethod: PaymentMethod,
    val permissions: List<Permission>,
) : JsonSerializable() {
    companion object : JsonSerializer<AccountData> {
        override fun fromJson(json: JSONObject): AccountData = AccountData(
            json.getLong("id"),
            json.getString("name"),
            json.getString("familyName"),
            json.getString("address"),
            json.getInt("postalCode"),
            json.getString("dni"),
            json.getDate("born"),
            json.getDate("registration"),
            json.getStringOrNull("phone"),
            json.getStringOrNull("workPhone"),
            json.getStringOrNull("mobilePhone"),
            json.getString("email"),
            json.getStringOrNull("profileImage"),
            json.getJSONObject("wheel").getJSONObjectOrNull("whites")?.serialize(Wheel.Companion),
            json.getJSONObject("wheel").getJSONObjectOrNull("blacks")?.serialize(Wheel.Companion),
            json.getIntOrNull("age"),
            json.getJSONObjectOrNull("trebuchet")?.serialize(TrebuchetData.Companion),
            FesterType.valueOf(json.getInt("type")),
            PaymentMethod.valueOf(json.getInt("payment")),
            json.getJSONArray("permissions").asStringList.map { Permission.valueOf(it) },
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("familyName", familyName)
        put("address", address)
        put("postalCode", postalCode)
        put("dni", dni)
        putDate("born", born)
        putDate("registration", registrationDate)
        put("phone", phone)
        put("workPhone", workPhone)
        put("mobilePhone", mobilePhone)
        put("email", email)
        put("profileImage", profileImage)
        put("wheel", JSONObject().apply {
            put("whites", whiteWheel?.toJson())
            put("blacks", blackWheel?.toJson())
        })
        put("age", age)
        put("trebuchet", trebuchetData?.toJson())
        put("type", type.dbType)
        put("payment", paymentMethod.id)
        put("permissions", JSONArray(permissions.map { it.name }))
    }

    val username: String
        get() = name.trim().lowercase().replaceFirstChar { it.uppercaseChar() }

    override fun equals(other: Any?): Boolean {
        if (other !is AccountData)
            return false
        return other.id == id
    }

    fun hasPermission(permission: Permission): Boolean = permissions.find { it == permission } != null
}
