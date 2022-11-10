package com.arnyminerz.filamagenta.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.ui.viewmodel.MainViewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer

@Composable
@ExperimentalMaterial3Api
fun TableSelectionDialog(
    event: EventEntity,
    viewModel: MainViewModel,
    isCreating: Boolean,
    isAdding: Boolean,
    onCreateRequest: () -> Unit,
    onSelectTable: (tableIndex: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_action_close))
            }
        },
        title = { Text(stringResource(R.string.choose_table_dialog_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(min = 0.dp, max = 300.dp),
            ) {
                Text(stringResource(R.string.choose_table_dialog_message))

                if (isCreating || isAdding)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(
                                if (isCreating)
                                    R.string.choose_table_dialog_creating
                                else
                                    R.string.choose_table_dialog_adding,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f),
                        )
                        CircularProgressIndicator()
                    }
                else {
                    val responsibles =
                        event.tables?.map { viewModel.getResponsibleData(it).observeAsState() }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        item {
                            ListItem(
                                headlineText = {
                                    Text(stringResource(R.string.choose_table_dialog_new_title))
                                },
                                supportingText = {
                                    Text(stringResource(R.string.choose_table_dialog_new_subtitle))
                                },
                                modifier = Modifier
                                    .clickable(onClick = onCreateRequest),
                            )
                        }
                        if (responsibles != null)
                            itemsIndexed(responsibles) { index, responsibleState ->
                                val responsible by responsibleState
                                ListItem(
                                    headlineText = {
                                        Text(
                                            responsible?.displayName ?: "----------",
                                            modifier = Modifier
                                                .placeholder(
                                                    visible = true,
                                                    highlight = PlaceholderHighlight.shimmer(),
                                                ),
                                        )
                                    },
                                    modifier = Modifier
                                        .clickable(enabled = responsible != null) {
                                            onSelectTable(index)
                                        },
                                )
                            }
                        else
                            items(listOf("", "", "", "")) {
                                ListItem(
                                    headlineText = {
                                        Text(
                                            "----------",
                                            modifier = Modifier
                                                .placeholder(
                                                    visible = true,
                                                    highlight = PlaceholderHighlight.shimmer(),
                                                ),
                                        )
                                    },
                                )
                            }
                    }
                }
            }
        }
    )
}
