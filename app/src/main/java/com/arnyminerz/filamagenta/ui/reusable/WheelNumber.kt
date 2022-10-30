package com.arnyminerz.filamagenta.ui.reusable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.data.account.Wheel

@Composable
fun RowScope.WheelNumber(@StringRes label: Int, wheel: Wheel?) {
    Card(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp),
    ) {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                wheel?.number?.toString() ?: "--",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 30.sp,
            )
            if (wheel?.locked == true)
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = stringResource(R.string.account_wheel_lock),
                    modifier = Modifier.size(16.dp),
                )
        }
    }
}
