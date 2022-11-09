package com.arnyminerz.filamagenta.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlin.reflect.KClass

fun <A : Activity> KClass<A>.intent(context: Context, options: Intent.() -> Unit = {}) =
    Intent(context, this.java).apply(options)
