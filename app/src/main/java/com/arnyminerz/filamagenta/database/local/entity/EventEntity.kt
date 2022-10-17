package com.arnyminerz.filamagenta.database.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.event.EventType
import com.arnyminerz.filamagenta.data.event.Menu
import com.arnyminerz.filamagenta.database.local.dao.EventsDao
import com.arnyminerz.filamagenta.utils.json
import com.arnyminerz.filamagenta.utils.serialize.DatabaseSerializer
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
    @ColumnInfo(name = "assistance") val assistance: List<Long>,
    @ColumnInfo(name = "type") val type: EventType,
) : EntityInt {
    companion object : DatabaseSerializer<EventEntity> {
        override fun fromDatabaseRow(row: Map<String, Any?>) = EventEntity(
            // Adds 1 since Room starts counting at 1, and SQL Server at 0
            row.getValue("id") as Long + 1,
            row.getValue("DisplayName") as String,
            row.getValue("Date") as Date,
            row.getValue("Contact") as String?,
            (row["Menu"] as String?)?.json?.let { Menu.fromJson(it) },
            row.getValue("Description") as String?,
            emptyList(),
            (row.getValue("Category") as Long).let { EventType.valueOf(it) ?: EventType.GENERIC },
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
     * Tries to get the table that matches a given person.
     * @author Arnau Mora
     * @since 20221015
     * @param eventsDao An [EventsDao] instance for fetching the tables data from.
     * @param personId The id of the person to search for.
     * @return `null` if the person doesn't have any table assigned. Its [TableEntity] otherwise.
     */
    suspend fun getTable(eventsDao: EventsDao, personId: Long) =
        eventsDao.getAllTables().find { it.people.contains(personId) }

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
        menu.price.let { price ->
            price.toList().find { it.first == festerType }?.second ?: price[FesterType.UNKNOWN]
        }

    fun hasCapability(capability: EventType.Capabilities) = type.capabilities.contains(capability)

    fun hasAnyCapability(vararg capabilities: EventType.Capabilities) =
        capabilities.any { hasCapability(it) }

    fun hasAllCapabilities(vararg capabilities: EventType.Capabilities) =
        capabilities.all { hasCapability(it) }
}
