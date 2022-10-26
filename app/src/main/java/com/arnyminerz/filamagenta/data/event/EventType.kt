package com.arnyminerz.filamagenta.data.event

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.arnyminerz.filamagenta.R

enum class EventType(
    val category: Long,
    val icon: ImageVector,
    val capabilities: Array<Capabilities>,
    @StringRes val localizedName: Int,
) {
    GENERIC(
        0L,
        Icons.Rounded.Event,
        emptyArray(),
        R.string.event_type_generic,
    ),
    EAT(
        1L,
        Icons.Rounded.Restaurant,
        arrayOf(Capabilities.TABLE, Capabilities.PAYMENT, Capabilities.MENU),
        R.string.event_type_eat,
    ),
    ENTRADETA(
        2L,
        Icons.Rounded.Groups,
        arrayOf(Capabilities.RESERVATION),
        R.string.event_type_entradeta,
    );

    companion object {
        fun valueOf(dbValue: Long) = values().find { it.category == dbValue }
    }

    enum class Capabilities {
        TABLE, PAYMENT, MENU, RESERVATION
    }
}
