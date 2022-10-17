package com.arnyminerz.filamagenta.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import java.util.Date

fun Context.toast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, textRes, duration).show()

@UiThread
fun Context.startAddToCalendar(title: String, description: String, begin: Date, end: Date?) =
    Intent(Intent.ACTION_EDIT).apply {
        type = "vnd.android.cursor.item/event"
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin.time)
        putExtra(CalendarContract.Events.ALL_DAY, end == null)
        if (end != null)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.time)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
    }.takeIf { it.resolveActivity(packageManager) != null }
        ?.also { startActivity(it) }
