package com.arnyminerz.filamagenta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.event.EventType.Capabilities.MENU
import com.arnyminerz.filamagenta.data.event.EventType.Capabilities.RESERVATION
import com.arnyminerz.filamagenta.data.event.EventType.Capabilities.TABLE
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.ui.dialog.ConfirmAssistanceDialog
import com.arnyminerz.filamagenta.ui.dialog.PricesDialog
import com.arnyminerz.filamagenta.ui.dialog.TableSelectionDialog
import com.arnyminerz.filamagenta.ui.reusable.TableMemberItem
import com.arnyminerz.filamagenta.ui.viewmodel.MainViewModel
import com.arnyminerz.filamagenta.utils.startAddToCalendar
import com.arnyminerz.filamagenta.utils.toast
import com.arnyminerz.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
@ExperimentalMaterial3Api
fun EventScreen(
    navController: NavController,
    viewModel: MainViewModel,
    event: EventEntity,
    dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val snackbarHostState = SnackbarHostState()

    var showPricesDialog by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }
    var showConfirmAssistanceDialog by remember { mutableStateOf(false) }

    if (event.hasCapability(MENU) && showPricesDialog)
        PricesDialog(
            onDismissRequest = { showPricesDialog = false },
            prices = event.menu!!.price,
        )

    val account by viewModel.accountData.observeAsState()
    val festerType = account?.type

    val people by viewModel.people.observeAsState()
    val tables by viewModel.tablesList.observeAsState()
    val table = account
        ?.id
        ?.let { accountId ->
            Timber.i("Event id: ${event.id}. Tables: ${tables?.map { it.eventId }}")
            tables?.find { it.eventId == event.id && (it.people.contains(accountId) || it.responsibleId == accountId) }
        }
    val assistanceConfirmed =
        table != null || account?.let { event.assistance.contains(it.id) } ?: false

    var isCreatingTable by remember { mutableStateOf(false) }
    var isAddingToTable by remember { mutableStateOf(false) }

    if (showTableDialog)
        TableSelectionDialog(
            viewModel,
            isAdding = isAddingToTable,
            isCreating = isCreatingTable,
            onCreateRequest = {
                if (account == null) scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.toast_error_account_null))
                } else {
                    isCreatingTable = true
                    viewModel.createNewTable(account!!, event).invokeOnCompletion {
                        isCreatingTable = false
                        showTableDialog = false
                    }
                }
            },
            onSelectTable = { tableEntity ->
                if (account == null) scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.toast_error_account_null))
                } else {
                    isAddingToTable = true
                    viewModel.addToTable(tableEntity, account!!).invokeOnCompletion {
                        isAddingToTable = false
                        showTableDialog = false
                    }
                }
            },
            onDismissRequest = { showPricesDialog = false },
        )
    if (showConfirmAssistanceDialog)
        ConfirmAssistanceDialog(event.name, { showConfirmAssistanceDialog = false }) {
            if (account == null) scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.toast_error_account_null))
            } else
                viewModel.confirmAssistance(event, account!!)
            showConfirmAssistanceDialog = false
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.name,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
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
                actions = {
                    if (event.hasAnyCapability(TABLE, RESERVATION))
                        IconButton(
                            enabled = !assistanceConfirmed,
                            onClick = {
                                if (event.hasCapability(TABLE))
                                    showTableDialog = true
                                else if (event.hasCapability(RESERVATION))
                                    showConfirmAssistanceDialog = true
                            },
                        ) {
                            Icon(
                                when (assistanceConfirmed) {
                                    true -> Icons.Rounded.EventAvailable
                                    false -> Icons.Rounded.EventBusy
                                },
                                stringResource(
                                    when (assistanceConfirmed) {
                                        true -> R.string.event_confirmed
                                        false -> R.string.event_unconfirmed
                                    },
                                ),
                            )
                        }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
            ) {
                AssistChip(
                    onClick = {
                        context.startAddToCalendar(
                            title = event.name,
                            description = "",
                            begin = event.date,
                            end = null,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.CalendarToday,
                            stringResource(R.string.event_date)
                        )
                    },
                    label = { Text(dateFormatter.format(event.date)) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                )
                AssistChip(
                    onClick = { /*TODO*/ },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Phone,
                            stringResource(R.string.event_contact)
                        )
                    },
                    label = { Text(stringResource(R.string.event_contact)) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                )
            }

            // DESCRIPTION CARD
            if (event.description != null)
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.event_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 8.dp, end = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MarkdownText(
                        event.description,
                        // style = MaterialTheme.typography.bodyLarge,
                        // modifier = Modifier
                        //    .fillMaxWidth()
                        //    .padding(start = 8.dp, bottom = 8.dp, end = 8.dp),
                    )
                }

            // TABLE CARD
            if (table != null)
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.event_table),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        TableMemberItem(table.responsibleId, people, account!!, true)

                        for (personId in table.people)
                            TableMemberItem(personId, people, account!!)
                    }
                }

            // MENU CARD
            if (event.hasCapability(MENU))
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    val menu = event.menu!!

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val price: Double = menu.price
                            .toList()
                            .find { it.first == festerType }
                            ?.second
                            ?: menu.price[FesterType.UNKNOWN]
                            ?: -1.0

                        Text(
                            text = stringResource(R.string.event_menu),
                            modifier = Modifier
                                .weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AssistChip(
                            onClick = { showPricesDialog = true },
                            label = {
                                Text(
                                    if (price < 0)
                                        stringResource(R.string.event_price_unknown)
                                    else if (price == 0.0)
                                        stringResource(R.string.event_price_included)
                                    else
                                        "%.2f €".format(price),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .fillMaxWidth(),
                    ) {
                        if (menu.coffee)
                            AssistChip(
                                onClick = { context.toast(R.string.toast_event_coffee_included) },
                                label = { Text(stringResource(R.string.event_menu_coffee)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Coffee,
                                        stringResource(R.string.event_menu_coffee)
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        if (menu.drink)
                            AssistChip(
                                onClick = { context.toast(R.string.toast_event_drink_included) },
                                label = { Text(stringResource(R.string.event_menu_drink)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.LocalDrink,
                                        stringResource(R.string.event_menu_drink)
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                    }
                    for (list in menu.rounds.filter { it.isNotEmpty() })
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                        ) {
                            for (plate in list)
                                ListItem(
                                    headlineText = { Text(plate) },
                                )
                        }
                }
        }
    }
}
