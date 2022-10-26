package com.arnyminerz.filamagenta.utils

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun doAsync(@WorkerThread block: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.IO).launch(block = block)

suspend fun ui(@WorkerThread block: suspend CoroutineScope.() -> Unit) =
    withContext(Dispatchers.Main, block)

suspend fun io(@MainThread block: suspend CoroutineScope.() -> Unit) =
    withContext(Dispatchers.IO, block)

fun ViewModel.launchIO(@WorkerThread block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(Dispatchers.IO, block = block)
