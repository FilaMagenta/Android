package com.arnyminerz.filamagenta.database.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.event.EventType
import com.arnyminerz.filamagenta.data.event.Menu
import com.arnyminerz.filamagenta.data.event.TableData
import com.arnyminerz.filamagenta.utils.asLongList
import com.arnyminerz.filamagenta.utils.getDate
import com.arnyminerz.filamagenta.utils.getJSONArrayOrNull
import com.arnyminerz.filamagenta.utils.getJSONObjectOrNull
import com.arnyminerz.filamagenta.utils.getStringOrNull
import com.arnyminerz.filamagenta.utils.serialize
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONObject
import java.util.Date

@Entity(
    tableName = "events",
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "contact") val contact: String?,
    @ColumnInfo(name = "menu") val menu: Menu?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "assistance") val assistance: List<Long>?,
    @ColumnInfo(name = "tables") val tables: List<TableData>?,
    @ColumnInfo(name = "type") val type: EventType,
) : EntityInt {
    companion object : JsonSerializer<EventEntity> {
        override fun fromJson(json: JSONObject): EventEntity = EventEntity(
            id = json.getLong("id"),
            name = json.getString("displayName"),
            date = json.getDate("date"),
            menu = json.getJSONObjectOrNull("menu")?.serialize(Menu.Companion),
            contact = json.getStringOrNull("contact"),
            description = json.getStringOrNull("description"),
            assistance = json.getJSONArrayOrNull("attending")?.asLongList,
            tables = json.getJSONArrayOrNull("tables")?.serialize(TableData.Companion),
            type = EventType.valueOf(json.getLong("category")) ?: EventType.GENERIC,
        )
    }

    init {
        check(!type.capabilities.contains(EventType.Capabilities.MENU) || menu != null) {
            "The event#$id has the PAYMENT capability but doesn't have any menu defined.\n" +
                    "Capabilities: ${type.capabilities.joinToString { it.name }}\n" +
                    "Menu: $menu"
        }
    }

    @Ignore
    val dbId: Long = id - 1

    override fun hashCode(): Int =
        id.hashCode() + name.hashCode() + date.hashCode() + contact.hashCode() + menu.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is EventEntity)
            return false
        return id == other.id
    }

    /**
     * Finds the price from [menu] for the given [FesterType].
     * @author Arnau Mora
     * @since 20221015
     * @param festerType The [FesterType] to search for.
     * @throws UnsupportedOperationException If [menu] is null. Can happen if the [type] doesn't
     * have the [EventType.Capabilities.MENU] capability.
     */
    @Throws(UnsupportedOperationException::class)
    fun getPriceFor(festerType: FesterType) = if (menu == null)
        throw UnsupportedOperationException("Menu is null for event#$id")
    else
        menu.pricing.let { price ->
            price.toList().find { it.first == festerType }?.second ?: price[FesterType.OTHER]
        }

    fun hasCapability(capability: EventType.Capabilities) = type.capabilities.contains(capability)

    fun hasAnyCapability(vararg capabilities: EventType.Capabilities) =
        capabilities.any { hasCapability(it) }

    fun hasAllCapabilities(vararg capabilities: EventType.Capabilities) =
        capabilities.all { hasCapability(it) }
}

fun List<EventEntity>.assists(userId: Long) =
    find { entity ->
        entity.tables?.find { it.members.contains(userId) } != null
    } != null
