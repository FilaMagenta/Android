package com.arnyminerz.filamagenta.utils

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun doAsync(@WorkerThread block: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.IO).launch(block = block)

suspend fun ui(@MainThread block: suspend CoroutineScope.() -> Unit) =
    withContext(Dispatchers.Main, block)
