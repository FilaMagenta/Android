package com.arnyminerz.filamagenta.data.event

import android.content.Context
import androidx.annotation.WorkerThread
import com.arnyminerz.filamagenta.database.local.AppDatabase
import com.arnyminerz.filamagenta.database.remote.RemoteInterface
import com.arnyminerz.filamagenta.utils.asLongList
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject

data class TableData(
    val responsibleId: Long,
    val members: List<Long>,
) : JsonSerializable() {
    companion object : JsonSerializer<TableData> {
        override fun fromJson(json: JSONObject): TableData = TableData(
            json.getLong("responsible"),
            json.getJSONArray("members").asLongList,
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("responsible", responsibleId)
        put("members", JSONArray(members))
    }

    @WorkerThread
    suspend fun getMembersData(context: Context, accountIndex: Int) = members.map { userId ->
        AppDatabase.getInstance(context)
            .peopleDao()
            .getById(userId)
            ?: RemoteInterface.getInstance(context).getAccountData(userId, accountIndex)
    }
}
