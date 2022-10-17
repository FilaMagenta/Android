package com.arnyminerz.filamagenta.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filamagenta.R

@Composable
fun ConfirmAssistanceDialog(eventName: String, onDismissRequest: () -> Unit, onConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.confirm_assistance_dialog_title)) },
        text = { Text(stringResource(R.string.confirm_assistance_dialog_message).format(eventName)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_action_close))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmed) { Text(stringResource(R.string.confirm_assistance_dialog_action)) }
        },
    )
}
