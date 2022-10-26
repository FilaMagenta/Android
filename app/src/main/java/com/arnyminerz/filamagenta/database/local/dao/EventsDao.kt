package com.arnyminerz.filamagenta.database.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filamagenta.database.local.entity.EventEntity

@Dao
interface EventsDao {
    @WorkerThread
    @Query("SELECT * FROM events")
    suspend fun getAll(): List<EventEntity>

    @WorkerThread
    @Query("SELECT * FROM events")
    fun getAllLive(): LiveData<List<EventEntity>>

    @WorkerThread
    @Query("SELECT * FROM events WHERE id=:id")
    suspend fun findById(id: Long): List<EventEntity>

    @Insert
    @WorkerThread
    @Throws(SQLiteConstraintException::class)
    suspend fun add(entity: EventEntity)

    @Update
    @WorkerThread
    @Throws(SQLiteConstraintException::class)
    suspend fun update(entity: EventEntity)

    @Delete
    @WorkerThread
    @Throws(SQLiteConstraintException::class)
    suspend fun remove(table: EventEntity)
}
