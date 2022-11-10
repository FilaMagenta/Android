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
    val id: Long,
    val responsibleId: Long,
    val members: List<Long>,
) : JsonSerializable() {
    companion object : JsonSerializer<TableData> {
        override fun fromJson(json: JSONObject): TableData = TableData(
            json.getLong("id"),
            json.getLong("responsible"),
            json.getJSONArray("members").asLongList,
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("responsible", responsibleId)
        put("members", JSONArray(members))
    }

    @WorkerThread
    suspend fun getResponsibleData(context: Context, token: String) =
        AppDatabase.getInstance(context)
            .peopleDao()
            .getById(responsibleId)
            ?: RemoteInterface.getInstance(context).getAccountData(token, responsibleId)

    @WorkerThread
    suspend fun getMembersData(context: Context, token: String) = members.map { userId ->
        AppDatabase.getInstance(context)
            .peopleDao()
            .getById(userId)
            ?: RemoteInterface.getInstance(context).getAccountData(token, userId)
    }
}
