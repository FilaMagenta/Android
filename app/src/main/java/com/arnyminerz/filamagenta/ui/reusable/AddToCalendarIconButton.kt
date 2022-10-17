package com.arnyminerz.filamagenta.ui.reusable

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.InsertInvitation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filamagenta.R
import java.util.Date

@Composable
@SuppressLint("QueryPermissionsNeeded")
fun AddToCalendarIconButton(
    title: String,
    description: String,
    date: Date,
) {
    val context = LocalContext.current

    val intent = Intent(Intent.ACTION_EDIT).apply {
        type = "vnd.android.cursor.item/event"
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.time)
        putExtra(CalendarContract.Events.ALL_DAY, true)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
    }.takeIf { it.resolveActivity(context.packageManager) != null }

    if (intent != null)
        IconButton(
            onClick = { intent.also { context.startActivity(it) } },
        ) {
            Icon(
                Icons.Rounded.InsertInvitation,
                stringResource(R.string.image_desc_add_to_calendar),
            )
        }
}
