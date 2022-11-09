package com.arnyminerz.filamagenta.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.data.account.AccountData
import com.arnyminerz.filamagenta.ui.reusable.AddToCalendarIconButton
import com.arnyminerz.filamagenta.ui.reusable.LabeledTextField
import com.arnyminerz.filamagenta.ui.reusable.WheelNumber

@ExperimentalMaterial3Api
@Composable
fun MainActivity.AccountScreen(account: AccountData) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    val accounts by viewModel.accountsList.observeAsState()
    val isAccountOneOfLoggedIn = accounts
        ?.mapNotNull { viewModel.findAccountDataByName(it.name) }
        ?.find { it.id == account.id } != null

    if (showSignOutDialog && isAccountOneOfLoggedIn)
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.close_session_confirm_dialog_title)) },
            text = { Text(stringResource(R.string.close_session_confirm_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel
                            .signOut(account, this)
                            .invokeOnCompletion { finish() }
                    },
                ) { Text(stringResource(R.string.close_session_confirm_dialog_action)) }
            },
            dismissButton = {
                Button(
                    onClick = { showSignOutDialog = false },
                ) { Text(stringResource(R.string.dialog_action_close)) }
            },
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_account)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            stringResource(R.string.image_desc_close),
                        )
                    }
                },
                actions = {
                    if (isAccountOneOfLoggedIn)
                        IconButton(
                            onClick = { showSignOutDialog = true },
                        ) {
                            Icon(
                                Icons.Rounded.Logout,
                                stringResource(R.string.image_desc_sign_out),
                            )
                        }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Image(
                // TODO: Load user image
                painterResource(R.drawable.logo_magenta),
                stringResource(R.string.image_desc_profile_image),
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape),
            )
            Text(
                text = account.name.lowercase().replaceFirstChar { it.uppercaseChar() } +
                        account.familyName
                            .lowercase()
                            .split(' ')
                            .joinToString(" ") {
                                it.replaceFirstChar { c -> c.uppercaseChar() }
                            },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                text = stringResource(account.type.localizedName),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .width(150.dp)
                    .padding(vertical = 2.dp)
                    .background(
                        account.type.color,
                        RoundedCornerShape(8.dp),
                    )
                    .align(Alignment.CenterHorizontally),
            )
            Row(
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, top = 24.dp, bottom = 8.dp),
            ) {
                WheelNumber(
                    label = R.string.account_wheel_white,
                    wheel = account.whiteWheel,
                )
                WheelNumber(
                    label = R.string.account_wheel_black,
                    wheel = account.blackWheel,
                )
                // WheelNumber(label = R.string.account_antiquity, number = account.age)
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(
                    stringResource(R.string.account_personal_data),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp),
                )
                LabeledTextField(account.address, R.string.account_personal_data_address)
                LabeledTextField(account.nif, R.string.account_personal_data_dni)
                LabeledTextField(account.born, R.string.account_personal_data_born)
                // LabeledTextField(
                //     account.registrationDate,
                //     R.string.account_personal_data_sign_up_date
                // )
                LabeledTextField(account.phone, R.string.account_personal_data_phone)
                LabeledTextField(
                    account.mobilePhone,
                    R.string.account_personal_data_mobile_phone
                )
                LabeledTextField(
                    account.workPhone,
                    R.string.account_personal_data_work_phone
                )
                LabeledTextField(account.email, R.string.account_personal_data_email)
                LabeledTextField(
                    account.paymentMethod.localizedName,
                    R.string.account_personal_data_payment_method
                )
            }
            if (account.trebuchetData != null)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Text(
                        stringResource(R.string.account_trebuchet),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp),
                    )
                    LabeledTextField(
                        account.trebuchetData.obtained,
                        R.string.account_trebuchet_date,
                    )
                    LabeledTextField(
                        account.trebuchetData.expires,
                        R.string.account_trebuchet_expiration,
                        trailing = account.trebuchetData.expires
                            ?.let { date ->
                                {
                                    AddToCalendarIconButton(
                                        stringResource(R.string.event_trebuchet_expiration_title),
                                        stringResource(R.string.event_trebuchet_expiration_summary),
                                        date,
                                    )
                                }
                            },
                    )
                }
            // Address, DNI, Born, Sign up, telfs, email, payment method, white number,
            // black number, glorier, avancarga, dispara, fecha carnet, env. ass, prot. datos
        }
    }
}
