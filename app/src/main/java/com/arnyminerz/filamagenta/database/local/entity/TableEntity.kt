package com.arnyminerz.filamagenta.database.local.entity

import androidx.annotation.WorkerThread
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.database.local.dao.EventsDao
import com.arnyminerz.filamagenta.database.local.dao.PeopleDao
import com.arnyminerz.filamagenta.utils.serialize.DatabaseSerializer

@Entity(
    tableName = "tables",
)
data class TableEntity(
    @PrimaryKey
    val id: Long = 0L,
    val responsibleId: Long,
    val eventId: Long,
    val people: List<Long>,
): EntityInt {
    companion object: DatabaseSerializer<TableEntity> {
        override fun fromDatabaseRow(row: Map<String, Any?>): TableEntity = TableEntity(
            row["Id"] as Long,
            row["Responsible"] as Long,
            row["Event"] as Long + 1,
            listOf(),
        )
    }

    @WorkerThread
    suspend fun findResponsible(dao: PeopleDao): SocioEntity? = dao
        .findById(responsibleId)
        .takeIf { it.isNotEmpty() }
        ?.get(0)

    @WorkerThread
    suspend fun findEvent(dao: EventsDao): EventEntity? = dao
        .findById(eventId)
        .takeIf { it.isNotEmpty() }
        ?.get(0)

    override fun hashCode(): Int = id.hashCode() + responsibleId.hashCode() + eventId.hashCode() +
            people.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is TableEntity)
            return false
        return other.id == id
    }

    /**
     * Returns if [people] contains [accountId] or [accountId] is equal to [responsibleId].
     * @author Arnau Mora
     * @since 20221015
     */
    fun hasPerson(accountId: Long) = people.contains(accountId) || responsibleId == accountId
}
