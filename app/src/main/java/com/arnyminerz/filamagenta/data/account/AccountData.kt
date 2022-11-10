package com.arnyminerz.filamagenta.data.account

import com.arnyminerz.filamagenta.utils.*
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class AccountData(
    val id: Long,
    val name: String,
    val familyName: String,
    val address: Address,
    val nif: String,
    val born: Date?,
    val phone: String?,
    val workPhone: String?,
    val mobilePhone: String?,
    val email: String?,
    val profileImage: String?,
    val whiteWheel: Wheel?,
    val blackWheel: Wheel?,
    val registration: Date?,
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
            json.getJSONObject("address", Address.Companion),
            json.getString("nif"),
            json.getDate("born"),
            json.getStringOrNull("phone"),
            json.getStringOrNull("workPhone"),
            json.getStringOrNull("mobilePhone"),
            json.getStringOrNull("email"),
            json.getStringOrNull("profileImage"),
            json.getJSONObject("wheel").getJSONObject("whites").serialize(Wheel.Companion),
            json.getJSONObject("wheel").getJSONObject("blacks").serialize(Wheel.Companion),
            json.getDateOrNull("registration"),
            json.getJSONObjectOrNull("trebuchet")?.serialize(TrebuchetData.Companion),
            FesterType.valueOf(json.getStringOrNull("type")),
            PaymentMethod.valueOf(json.getIntOrNull("payment")),
            json.getJSONArray("permissions").asStringList.map { Permission.valueOf(it) },
        )

        fun fromRest(json: JSONObject): AccountData =
            json.getJSONObject("vCard").let { vCard ->
                val name = vCard.getJSONArray("name").asStringList
                val telephones = vCard.getJSONArray("telephones")
                    .asArrayList
                    .associate { it[0] as String to it[1] as String }

                AccountData(
                    json.getLong("Id"),
                    name[0],
                    name[1],
                    vCard.getJSONArray("address")
                        .asStringList
                        .let { Address(it[0], it[1].split(";")) },
                    json.getString("NIF"),
                    vCard.getDateOrNull("birthday", DayDateFormat),
                    telephones["home"],
                    telephones["work"],
                    telephones["cell"],
                    vCard.getString("email"),
                    vCard.getStringOrNull("photo"),
                    Wheel(
                        json.getJSONObject("Grade").getBoolean("LockWhitesWheel"),
                        json.getLong("WhitesWheelNumber"),
                    ),
                    Wheel(
                        json.getJSONObject("Grade").getBoolean("LockBlacksWheel"),
                        json.getLong("BlacksWheelNumber"),
                    ),
                    json.getStringOrNull("Registration")
                        ?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it) },
                    json.getJSONObjectOrNull("trebuchet")?.serialize(TrebuchetData.Companion),
                    FesterType.valueOf(json.getStringOrNull("type")),
                    PaymentMethod.valueOf(json.getIntOrNull("payment")),
                    json.getJSONArrayOrNull("permissions")
                        ?.asStringList
                        ?.map { Permission.valueOf(it) }
                        ?: emptyList(),
                )
            }
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("familyName", familyName)
        put("address", address.toJson())
        put("nif", nif)
        putDate("born", born)
        put("phone", phone)
        put("workPhone", workPhone)
        put("mobilePhone", mobilePhone)
        put("email", email)
        put("profileImage", profileImage)
        put("wheel", JSONObject().apply {
            put("whites", whiteWheel?.toJson())
            put("blacks", blackWheel?.toJson())
        })
        putDate("registration", registration)
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

    fun hasPermission(permission: Permission): Boolean =
        permissions.find { it == permission } != null
}
