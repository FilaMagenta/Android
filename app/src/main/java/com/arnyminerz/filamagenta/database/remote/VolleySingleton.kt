package com.arnyminerz.filamagenta.database.remote

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import java.util.UUID

class VolleySingleton private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolleySingleton(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>): UUID {
        val uuid = UUID.randomUUID()
        req.tag = uuid.toString()

        requestQueue.add(req)

        return uuid
    }

    fun cancel(uuid: UUID) = requestQueue.cancelAll(uuid.toString())
}