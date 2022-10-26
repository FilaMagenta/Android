package com.arnyminerz.filamagenta.database.local.dao

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData

@Dao
interface PeopleDao {
    @WorkerThread
    @Insert
    suspend fun add(personData: ShortPersonData)

    @WorkerThread
    @Query("SELECT * FROM people WHERE id=:id")
    suspend fun getById(id: Long): ShortPersonData?
}