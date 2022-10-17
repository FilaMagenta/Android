package com.arnyminerz.filamagenta.database.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.arnyminerz.filamagenta.database.local.entity.SocioEntity

@Dao
interface PeopleDao {
    @WorkerThread
    @Query("SELECT * FROM people")
    suspend fun getAll(): List<SocioEntity>

    @WorkerThread
    @Query("SELECT * FROM people")
    fun getAllLive(): LiveData<List<SocioEntity>>

    @WorkerThread
    @Query("SELECT * FROM people WHERE id=:id")
    suspend fun findById(id: Long): List<SocioEntity>

    @Insert
    @WorkerThread
    @Throws(SQLiteConstraintException::class)
    suspend fun add(entity: SocioEntity)
}
