package com.arnyminerz.filamagenta.database.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.data.account.Address
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.account.PaymentMethod
import com.arnyminerz.filamagenta.data.account.Permission
import com.arnyminerz.filamagenta.data.account.TrebuchetData
import com.arnyminerz.filamagenta.data.account.Wheel
import com.arnyminerz.filamagenta.utils.DayDateFormat
import com.arnyminerz.filamagenta.utils.asArrayList
import com.arnyminerz.filamagenta.utils.asStringList
import com.arnyminerz.filamagenta.utils.getDate
import com.arnyminerz.filamagenta.utils.getDateOrNull
import com.arnyminerz.filamagenta.utils.getIntOrNull
import com.arnyminerz.filamagenta.utils.getJSONArrayOrNull
import com.arnyminerz.filamagenta.utils.getJSONObject
import com.arnyminerz.filamagenta.utils.getJSONObjectOrNull
import com.arnyminerz.filamagenta.utils.getStringOrNull
import com.arnyminerz.filamagenta.utils.putDate
import com.arnyminerz.filamagenta.utils.serialize
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "people_data"
)
data class PersonData(
    @PrimaryKey val id: Long,
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
) : EntityInt, JsonSerializable() {
    companion object : JsonSerializer<PersonData> {
        override fun fromJson(json: JSONObject): PersonData = PersonData(
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

        fun fromRest(json: JSONObject): PersonData =
            json.getJSONObject("vCard").let { vCard ->
                val name = vCard.getJSONArray("name").asStringList
                val telephones = vCard.getJSONArray("telephones")
                    .asArrayList
                    .associate { it[0] as String to it[1] as String }
                val grade = json.getJSONObject("Grade")

                PersonData(
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
                        grade.getBoolean("LockWhitesWheel"),
                        json.getLong("WhitesWheelNumber"),
                    ),
                    Wheel(
                        grade.getBoolean("LockBlacksWheel"),
                        json.getLong("BlacksWheelNumber"),
                    ),
                    json.getStringOrNull("Registration")
                        ?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it) },
                    json.getJSONObjectOrNull("trebuchet")?.serialize(TrebuchetData.Companion),
                    FesterType.valueOf(grade.getStringOrNull("DisplayName")),
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

    fun hasPermission(permission: Permission): Boolean =
        permissions.find { it == permission } != null

    val short: ShortPersonData
        get() = ShortPersonData(id = id, displayName = "$name $familyName")
}