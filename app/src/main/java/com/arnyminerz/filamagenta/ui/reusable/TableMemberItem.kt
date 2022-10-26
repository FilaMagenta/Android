package com.arnyminerz.filamagenta.ui.reusable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.data.account.AccountData
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData

@Composable
@ExperimentalMaterial3Api
fun TableMemberItem(
    personId: Long,
    people: List<ShortPersonData>?,
    loggedInAccount: AccountData,
    isResponsible: Boolean = false,
) {
    ListItem(
        headlineText = {
            Text(
                people?.find { it.id == personId }?.displayName ?: "Err"
            )
        },
        supportingText = {
            if (isResponsible)
                Text(stringResource(R.string.event_table_responsible))
        },
        leadingContent = {
            if (isResponsible)
                Icon(
                    Icons.Rounded.StarOutline,
                    stringResource(R.string.event_table_responsible)
                )
        },
        trailingContent = {
            if (loggedInAccount.id == personId)
                Badge {
                    Text(stringResource(R.string.badge_you))
                }
        },
    )
}
