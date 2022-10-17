package com.arnyminerz.filamagenta.ui.reusable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.event.EventType
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventCard(
    event: EventEntity,
    dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
    assistanceConfirmed: Boolean,
    festerType: FesterType,
    onViewRequested: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    event.type.icon,
                    event.type.name,
                )
                Text(
                    text = event.name,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                Icon(
                    if (assistanceConfirmed)
                        Icons.Rounded.Check
                    else
                        Icons.Rounded.QuestionMark,
                    if (assistanceConfirmed)
                        stringResource(R.string.event_confirmed)
                    else
                        stringResource(R.string.event_unconfirmed),
                    tint = if (assistanceConfirmed)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Unspecified,
                )
            }
            Text(
                text = dateFormatter.format(event.date),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = onViewRequested,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .weight(1f),
                ) { Text(stringResource(R.string.event_view)) }
                if (event.hasCapability(EventType.Capabilities.PAYMENT))
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier
                            .padding(start = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            disabledContentColor = MaterialTheme.colorScheme.onBackground,
                        )
                    ) {
                        Text(
                            text = when (val price = event.getPriceFor(festerType)) {
                                0.0 -> stringResource(R.string.event_price_included)
                                null -> stringResource(R.string.event_price_unknown)
                                else -> "%.2f €".format(price)
                            },
                        )
                    }
            }
        }
    }
}
