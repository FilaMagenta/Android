package com.arnyminerz.filamagenta.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.data.event.EventType
import com.arnyminerz.filamagenta.ui.reusable.LabeledTextField
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import net.sourceforge.jtds.jdbc.DateTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Preview(
    showSystemUi = true,
    showBackground = true,
)
@Composable
@ExperimentalMaterial3Api
fun MainActivity.EventAddScreen() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }

    val dateDialogState = rememberMaterialDialogState()
    val timeDialogState = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            negativeButton(res = R.string.dialog_action_close)
            positiveButton(res = R.string.dialog_action_ok) {
                timeDialogState.show()
            }
        },
    ) { datepicker(date) { date = it } }
    MaterialDialog(
        dialogState = timeDialogState,
        buttons = {
            negativeButton(res = R.string.dialog_action_close)
            positiveButton(res = R.string.dialog_action_ok)
        },
    ) { timepicker(time, is24HourClock = true) { time = it } }

    var type by remember { mutableStateOf(EventType.GENERIC) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.event_new_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            stringResource(R.string.image_desc_go_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            var name by remember { mutableStateOf("") }

            Text(
                stringResource(R.string.event_new_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.event_new_section_parameters),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            LabeledTextField(
                value = name,
                label = R.string.event_new_name,
                supportingTextRes = R.string.event_new_name_description,
                onValueChange = { name = it },
            )
            LabeledTextField(
                value = date.format(DateTimeFormatter.ISO_DATE) + " " +
                        time.format(DateTimeFormatter.ofPattern("HH:mm")),
                label = R.string.event_new_date,
                isDisabled = true,
                modifier = Modifier
                    .clickable { dateDialogState.show() },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledLabelColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
            Box {
                var showingTypeDropdown by remember { mutableStateOf(false) }
                LabeledTextField(
                    value = stringResource(type.localizedName),
                    label = R.string.event_new_type,
                    isDisabled = true,
                    modifier = Modifier
                        .clickable { showingTypeDropdown = true },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                        disabledTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledLabelColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
                DropdownMenu(
                    expanded = showingTypeDropdown,
                    onDismissRequest = { showingTypeDropdown = false },
                ) {
                    for (t in EventType.values())
                        DropdownMenuItem(
                            text = { Text(stringResource(t.localizedName)) },
                            onClick = { type = t; showingTypeDropdown = false },
                        )
                }
            }
            AnimatedVisibility(visible = type == EventType.EAT) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val starters = remember { mutableStateListOf<String>() }
                    val firsts = remember { mutableStateListOf<String>() }
                    val seconds = remember { mutableStateListOf<String>() }
                    val desserts = remember { mutableStateListOf<String>() }
                    var drinksIncluded by remember { mutableStateOf(false) }
                    var coffeeIncluded by remember { mutableStateOf(false) }

                    Text(
                        stringResource(R.string.event_new_section_menu),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FilterChip(
                        selected = drinksIncluded,
                        onClick = { drinksIncluded = !drinksIncluded },
                        label = { Text(stringResource(R.string.event_new_menu_drink_included)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { /*TODO*/ },
                            ) {
                                Icon(
                                    Icons.Rounded.QuestionMark,
                                    stringResource(R.string.image_desc_help),
                                )
                            }
                        },
                    )
                    FilterChip(
                        selected = coffeeIncluded,
                        onClick = { coffeeIncluded = !coffeeIncluded },
                        label = { Text(stringResource(R.string.event_new_menu_coffee_included)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { /*TODO*/ },
                            ) {
                                Icon(
                                    Icons.Rounded.QuestionMark,
                                    stringResource(R.string.image_desc_help),
                                )
                            }
                        },
                    )
                    MenuPartCard(
                        titleRes = R.string.event_new_section_menu_starters,
                        plates = starters,
                    )
                    MenuPartCard(
                        titleRes = R.string.event_new_section_menu_firsts,
                        plates = firsts,
                    )
                    MenuPartCard(
                        titleRes = R.string.event_new_section_menu_seconds,
                        plates = seconds,
                    )
                    MenuPartCard(
                        titleRes = R.string.event_new_section_menu_dessert,
                        plates = desserts,
                    )
                }
            }
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun MenuPartCard(@StringRes titleRes: Int, plates: SnapshotStateList<String>) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        var currentPlateName by remember { mutableStateOf("") }
        Text(
            stringResource(titleRes),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleSmall,
        )
        LabeledTextField(
            value = currentPlateName,
            label = R.string.event_new_menu_plate_name,
            onValueChange = { currentPlateName = it },
            trailing = {
                IconButton(
                    onClick = {
                        plates.add(currentPlateName)
                        currentPlateName = ""
                    }
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        stringResource(R.string.image_desc_add_to_menu)
                    )
                }
            }
        )
        LazyColumn {
            items(plates) { plate ->
                ListItem(
                    headlineText = { Text(plate) },
                    trailingContent = {
                        IconButton(
                            onClick = { plates.remove(plate) },
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                stringResource(R.string.image_desc_remove),
                            )
                        }
                    },
                )
            }
        }
    }
}
