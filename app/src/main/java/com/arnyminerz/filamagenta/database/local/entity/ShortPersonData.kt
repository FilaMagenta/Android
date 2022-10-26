package com.arnyminerz.filamagenta.database.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.utils.getStrings
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONObject
import kotlin.random.Random

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

@Entity(
    tableName = "people"
)
data class ShortPersonData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val displayName: String,
) : EntityInt {
    companion object : JsonSerializer<ShortPersonData> {
        override fun fromJson(json: JSONObject): ShortPersonData = ShortPersonData(
            displayName = json.getStrings(" ", "name", "familyName")
        )

        fun randomPlaceholder(count: Long, maxWordLength: Long = 7) = (0 until count)
            .map {
                (0 until maxWordLength)
                    .map { Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")
            }
            .map { ShortPersonData(displayName = it) }
    }
}
