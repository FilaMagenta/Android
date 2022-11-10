package com.arnyminerz.filamagenta.database.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arnyminerz.filamagenta.database.local.dao.EventsDao
import com.arnyminerz.filamagenta.database.local.dao.PeopleDao
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.database.local.entity.PersonData
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData

@Database(
    entities = [EventEntity::class, ShortPersonData::class, PersonData::class],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "fila_magenta")
                .build()
                .also { INSTANCE = it }
        }
    }

    abstract fun eventsDao(): EventsDao

    abstract fun peopleDao(): PeopleDao
}
