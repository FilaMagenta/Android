package com.arnyminerz.filamagenta.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.EmojiFoodBeverage
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material3.Divider
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.event.EventType
import com.arnyminerz.filamagenta.ui.reusable.LabeledTextField
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
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
        },
        contentWindowInsets = WindowInsets.ime,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .fillMaxSize()
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
                    var teaIncluded by remember { mutableStateOf(false) }
                    val pricing = remember { mutableStateMapOf<FesterType, Double>() }
                    var currentType by remember { mutableStateOf(FesterType.OTHER) }
                    var currentPrice by remember { mutableStateOf("") }

                    Divider(Modifier.padding(vertical = 8.dp))

                    Text(
                        stringResource(R.string.event_new_section_menu),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        FilterChip(
                            selected = drinksIncluded,
                            onClick = { drinksIncluded = !drinksIncluded },
                            label = { Text(stringResource(R.string.event_new_menu_drink_included)) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.LocalDrink,
                                    stringResource(R.string.event_new_menu_drink_included)
                                )
                            },
                        )
                        FilterChip(
                            selected = coffeeIncluded,
                            onClick = { coffeeIncluded = !coffeeIncluded },
                            label = { Text(stringResource(R.string.event_new_menu_coffee_included)) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Coffee,
                                    stringResource(R.string.event_new_menu_coffee_included)
                                )
                            },
                        )
                        FilterChip(
                            selected = teaIncluded,
                            onClick = { teaIncluded = !teaIncluded },
                            label = { Text(stringResource(R.string.event_new_menu_tea_included)) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.EmojiFoodBeverage,
                                    stringResource(R.string.event_new_menu_tea_included)
                                )
                            },
                        )
                    }
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

                    Divider(Modifier.padding(vertical = 8.dp))

                    // PRICING
                    Text(
                        stringResource(R.string.event_new_section_pricing),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .padding(bottom = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.3f),
                            ) {
                                var showingTypeDropdown by remember { mutableStateOf(false) }
                                LabeledTextField(
                                    value = stringResource(currentType.localizedName),
                                    label = R.string.event_new_pricing_type,
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
                                    for (t in FesterType.values())
                                        DropdownMenuItem(
                                            text = { Text(stringResource(t.localizedName)) },
                                            onClick = {
                                                currentType = t; showingTypeDropdown = false
                                            },
                                            enabled = !pricing.containsKey(t),
                                        )
                                }
                            }
                            fun newRow() {
                                if (currentPrice.toDoubleOrNull() == null) return

                                pricing[currentType] = currentPrice.toDouble()
                                currentType = FesterType.values()
                                    .find { !pricing.containsKey(it) }
                                    ?: FesterType.OTHER
                                currentPrice = ""
                            }
                            LabeledTextField(
                                value = currentPrice,
                                label = R.string.event_new_pricing_price,
                                onValueChange = {
                                    if (it.toDoubleOrNull() != null)
                                        currentPrice = it
                                },
                                modifier = Modifier
                                    .weight(1f),
                                trailing = {
                                    Icon(
                                        Icons.Rounded.EuroSymbol,
                                        stringResource(R.string.event_new_pricing_price),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { newRow() },
                                ),
                            )
                            IconButton(
                                onClick = { newRow() },
                                enabled = currentPrice.toDoubleOrNull() != null,
                            ) {
                                Icon(Icons.Rounded.ChevronRight, "")
                            }
                        }
                        for ((t, price) in pricing)
                            ListItem(
                                headlineText = { Text(stringResource(t.localizedName)) },
                                supportingText = {
                                    Text(
                                        price.takeIf { it > 0 }
                                            ?.toString()
                                            ?: stringResource(R.string.event_price_included),
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = { pricing.remove(t) }) {
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
    }
}

@Composable
@ExperimentalMaterial3Api
fun MenuPartCard(@StringRes titleRes: Int, plates: SnapshotStateList<String>) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        var currentPlateName by remember { mutableStateOf("") }
        Text(
            stringResource(titleRes),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.titleSmall,
        )
        LabeledTextField(
            value = currentPlateName,
            label = R.string.event_new_menu_plate_name,
            onValueChange = { currentPlateName = it },
            trailing = {
                IconButton(
                    onClick = {
                        if (currentPlateName.isNotBlank())
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
        Column {
            plates.forEach { plate ->
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
