package com.arnyminerz.filamagenta.database.local.dao

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filamagenta.database.local.entity.PersonData
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData

@Dao
interface PeopleDao {
    @WorkerThread
    @Insert
    suspend fun add(personData: ShortPersonData)

    @WorkerThread
    @Query("SELECT * FROM people WHERE id=:id")
    suspend fun getById(id: Long): ShortPersonData?

    @Insert
    @WorkerThread
    suspend fun add(data: PersonData)

    @WorkerThread
    @Query("SELECT * FROM people_data WHERE id=:id")
    suspend fun getDataById(id: Long): PersonData?

    @WorkerThread
    @Query("SELECT * FROM people_data WHERE nif=:nif")
    suspend fun getDataByNif(nif: String): PersonData?

    @Update
    @WorkerThread
    suspend fun update(data: PersonData)
}