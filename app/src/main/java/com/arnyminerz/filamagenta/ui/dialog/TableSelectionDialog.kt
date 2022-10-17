package com.arnyminerz.filamagenta.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.database.local.entity.TableEntity
import com.arnyminerz.filamagenta.ui.reusable.LoadingIndicatorBox
import com.arnyminerz.filamagenta.ui.viewmodel.MainViewModel
import com.google.accompanist.placeholder.material.placeholder

@Composable
@ExperimentalMaterial3Api
fun TableSelectionDialog(
    viewModel: MainViewModel,
    isCreating: Boolean,
    isAdding: Boolean,
    onCreateRequest: () -> Unit,
    onSelectTable: (table: TableEntity) -> Unit,
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
                    .heightIn(max = 300.dp),
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
                    val people by viewModel.people.observeAsState(null)
                    val tablesList by viewModel.tablesList.observeAsState(null)
                    if (tablesList == null || people == null)
                        LoadingIndicatorBox()
                    else
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            items(tablesList ?: emptyList()) { table ->
                                val responsible = people?.find { it.id == table.responsibleId }
                                ListItem(
                                    headlineText = { Text(responsible?.displayName ?: "-----------") },
                                    modifier = Modifier
                                        .placeholder(visible = responsible == null)
                                        .clickable(enabled = responsible != null) {
                                            onSelectTable(
                                                table
                                            )
                                        },
                                )
                            }
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
                        }
                }
            }
        }
    )
}
