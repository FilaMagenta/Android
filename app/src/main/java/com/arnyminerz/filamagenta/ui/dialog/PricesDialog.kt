package com.arnyminerz.filamagenta.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.data.account.FesterType

@Composable
@ExperimentalMaterial3Api
fun PricesDialog(
    onDismissRequest: () -> Unit,
    prices: Map<FesterType, Double>,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.prices_dialog_title)) },
        text = {
            Column {
                for ((type, price) in prices)
                    ListItem(
                        headlineText = {
                            Text(
                                stringResource(
                                    when (type) {
                                        FesterType.OTHER -> R.string.fester_type_external
                                        else -> type.localizedName
                                    }
                                )
                            )
                        },
                        trailingContent = {
                            Text(
                                when {
                                    price < 0 -> stringResource(R.string.event_price_unknown)
                                    price == 0.0 -> stringResource(R.string.event_price_included)
                                    else -> "%.2f €".format(price)
                                }
                            )
                                          },
                    )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_action_close))
            }
        },
    )
}

private const val PRICE_EXAMPLE_FESTER = 0.0
private const val PRICE_EXAMPLE_SIT_ESP = 20.0
private const val PRICE_EXAMPLE_UNKNOWN = 30.0

@Preview
@Composable
@ExperimentalMaterial3Api
fun PricesDialogPreview() {
    PricesDialog(
        onDismissRequest = {},
        prices = mapOf(
            FesterType.FESTER to PRICE_EXAMPLE_FESTER,
            FesterType.SIT_ESP to PRICE_EXAMPLE_SIT_ESP,
            FesterType.OTHER to PRICE_EXAMPLE_UNKNOWN,
        ),
    )
}
