package com.arnyminerz.filamagenta.utils

import android.os.Bundle

@Suppress("DEPRECATION")
fun Bundle.toMap() = keySet().associateWith { get(it) }
