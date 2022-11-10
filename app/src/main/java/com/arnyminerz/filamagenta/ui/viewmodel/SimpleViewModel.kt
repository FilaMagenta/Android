package com.arnyminerz.filamagenta.ui.viewmodel

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.arnyminerz.filamagenta.utils.launchIO
import kotlinx.coroutines.CoroutineScope

class SimpleViewModel : ViewModel() {
    fun invoke(@WorkerThread block: suspend CoroutineScope.() -> Unit) = launchIO(block)
}