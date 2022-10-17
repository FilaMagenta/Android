package com.arnyminerz.filamagenta.data.account

import com.arnyminerz.filamagenta.utils.asStringList
import com.arnyminerz.filamagenta.utils.getDate
import com.arnyminerz.filamagenta.utils.getDateOrNull
import com.arnyminerz.filamagenta.utils.getIntOrNull
import com.arnyminerz.filamagenta.utils.getStringOrNull
import com.arnyminerz.filamagenta.utils.putDate
import com.arnyminerz.filamagenta.utils.serialize.DatabaseSerializer
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
    val whiteWheelNumber: Int?,
    val blackWheelNumber: Int?,
    val age: Int?,
    val trebuchetObtained: Boolean,
    val trebuchetShoots: Boolean,
    val trebuchetDate: Date?,
    val trebuchetExpiration: Date?,
    val type: FesterType,
    val paymentMethod: PaymentMethod,
    val permissions: List<Permission>,
) : JsonSerializable() {
    companion object : DatabaseSerializer<AccountData>, JsonSerializer<AccountData> {
        override fun fromDatabaseRow(row: Map<String, Any?>): AccountData = AccountData(
            id = row["idSocio"] as Long,
            name = row["Nombre"] as String,
            familyName = row["Apellidos"] as String,
            address = row["Direccion"] as String,
            postalCode = row["idCodPostal"] as Int,
            dni = row["Dni"] as String,
            born = row["FecNacimiento"] as Date,
            registrationDate = row["FecAlta"] as Date,
            phone = row["TlfParticular"] as String?,
            workPhone = row["TlfTrabajo"] as String?,
            mobilePhone = row["TlfMovil"] as String?,
            email = row["eMail"] as String,
            profileImage = row["Fotografia"] as String?,
            whiteWheelNumber = row["nrRodaBlancos"] as Int?,
            blackWheelNumber = row["nrRodaNegros"] as Int?,
            age = row["nrAntiguedad"] as Int?,
            trebuchetObtained = row["bCarnetAvancarga"] as Boolean,
            trebuchetShoots = row["bDisparaAvancarga"] as Boolean,
            trebuchetDate = row["FecExpedicionAvancarga"] as Date?,
            trebuchetExpiration = row["FecCaducidadAvancarga"] as Date?,
            type = FesterType.valueOf(row["idTipoFestero"] as Int? ?: -1),
            paymentMethod = PaymentMethod.valueOf(row["idFormaPago"] as Int? ?: -1),
            permissions = emptyList(),
        )

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
            json.getIntOrNull("whiteWheel"),
            json.getIntOrNull("blackWheel"),
            json.getIntOrNull("age"),
            json.getBoolean("trebuchetObtained"),
            json.getBoolean("trebuchetShoots"),
            json.getDateOrNull("trebuchetDate"),
            json.getDateOrNull("trebuchetExpiration"),
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
        put("whiteWheel", whiteWheelNumber)
        put("blackWheel", blackWheelNumber)
        put("age", age)
        put("trebuchetObtained", trebuchetObtained)
        put("trebuchetShoots", trebuchetShoots)
        putDate("trebuchetDate", trebuchetDate)
        putDate("trebuchetExpiration", trebuchetExpiration)
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
