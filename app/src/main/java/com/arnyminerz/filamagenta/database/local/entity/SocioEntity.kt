package com.arnyminerz.filamagenta.database.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.account.PaymentMethod
import com.arnyminerz.filamagenta.utils.serialize.DatabaseSerializer
import java.util.Date

@Entity(
    tableName = "people",
)
data class SocioEntity(
    @PrimaryKey val id: Long = 0L,
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
    val email: String?,
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
) : EntityInt {
    companion object : DatabaseSerializer<SocioEntity> {
        override fun fromDatabaseRow(row: Map<String, Any?>) = SocioEntity(
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
            email = row["eMail"] as String?,
            profileImage = row["Fotografia"] as String?,
            whiteWheelNumber = row["nrRodaBlancos"] as Int?,
            blackWheelNumber = row["nrRodaNegros"] as Int?,
            age = row["nrAntiguedad"] as Int?,
            trebuchetObtained = row["bCarnetAvancarga"] as Boolean,
            trebuchetShoots = row["bDisparaAvancarga"] as Boolean,
            trebuchetDate = row["FecExpedicionAvancarga"] as Date?,
            trebuchetExpiration = row["FecCaducidadAvancarga"] as Date?,
            type = FesterType.valueOf(row["idTipoFestero"] as Int? ?: -1),
            paymentMethod = PaymentMethod.valueOf(row["idFormaPago"] as Int? ?: -1)
        )
    }

    @Ignore
    val displayName: String = listOf(
        name.lowercase().replaceFirstChar { it.uppercaseChar() },
        familyName
            .lowercase()
            .split(' ')
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
    ).joinToString(" ")

    override fun equals(other: Any?): Boolean {
        if (other !is SocioEntity)
            return false
        return other.id == id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + familyName.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + postalCode
        result = 31 * result + dni.hashCode()
        result = 31 * result + born.hashCode()
        result = 31 * result + registrationDate.hashCode()
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (workPhone?.hashCode() ?: 0)
        result = 31 * result + (mobilePhone?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (profileImage?.hashCode() ?: 0)
        result = 31 * result + (whiteWheelNumber ?: 0)
        result = 31 * result + (blackWheelNumber ?: 0)
        result = 31 * result + (age ?: 0)
        result = 31 * result + trebuchetObtained.hashCode()
        result = 31 * result + trebuchetShoots.hashCode()
        result = 31 * result + (trebuchetDate?.hashCode() ?: 0)
        result = 31 * result + (trebuchetExpiration?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + paymentMethod.hashCode()
        result = 31 * result + displayName.hashCode()
        return result
    }
}
