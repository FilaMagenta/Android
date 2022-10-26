package com.arnyminerz.filamagenta.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.ui.backend.WindowSizeClass
import com.arnyminerz.filamagenta.ui.backend.computeWindowSizeClasses
import com.arnyminerz.filamagenta.ui.reusable.LoginField
import com.arnyminerz.filamagenta.ui.theme.CardWithLogo
import com.arnyminerz.filamagenta.ui.theme.JostFontFamily
import com.arnyminerz.filamagenta.utils.isValidDNI
import kotlinx.coroutines.Job

@Preview(
    showSystemUi = true,
    showBackground = true,
)
@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
fun LoginScreen(
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onLoginRequested: ((username: String, password: String) -> Job)? = null,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val size = computeWindowSizeClasses()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.ime,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CardWithLogo(
                cardModifier = Modifier
                    .align(Alignment.Center)
                    .padding(paddingValues)
                    .padding(
                        vertical = 32.dp,
                    )
                    .then(
                        if (size == WindowSizeClass.EXPANDED)
                            Modifier.width(500.dp)
                        else
                            Modifier
                                .padding(
                                    horizontal = 12.dp.takeIf { size == WindowSizeClass.COMPACT }
                                        ?: 36.dp,
                                )
                    ),
                contentModifier = Modifier,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 5.dp,
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                var fieldsEnabled by remember { mutableStateOf(true) }
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                var usernameError: String? by remember { mutableStateOf(null) }
                var passwordError: String? by remember { mutableStateOf(null) }

                val passwordFocusRequest = FocusRequester()

                fun performLogin() {
                    try {
                        fieldsEnabled = false

                        if (username.isEmpty())
                            usernameError = context.getString(R.string.login_error_empty_username)
                        if (password.isEmpty())
                            passwordError = context.getString(R.string.login_error_empty_password)
                        else if (!password.isValidDNI)
                            passwordError = context.getString(R.string.login_error_invalid_password)

                        if (usernameError != null || passwordError != null)
                            return

                        keyboardController?.hide()
                        onLoginRequested
                            ?.invoke(username, password)
                            ?.invokeOnCompletion { fieldsEnabled = true }
                    } finally {
                        if (onLoginRequested == null)
                            fieldsEnabled = true
                    }
                }

                Text(
                    "Login",
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = JostFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                )
                LoginField(
                    text = username,
                    onTextChange = { username = it; usernameError = null },
                    enabled = fieldsEnabled,
                    label = R.string.login_username,
                    error = usernameError,
                    onClearErrorRequested = { usernameError = null },
                    onActionPerformed = { passwordFocusRequest.requestFocus() },
                )
                LoginField(
                    text = password,
                    onTextChange = { password = it; passwordError = null },
                    enabled = fieldsEnabled,
                    label = R.string.login_password,
                    error = passwordError,
                    onClearErrorRequested = { passwordError = null },
                    isPasswordField = true,
                    actionNext = false,
                    onActionPerformed = { performLogin() },
                    focusRequester = passwordFocusRequest,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedVisibility(
                        visible = !fieldsEnabled,
                        modifier = Modifier
                            .padding(end = 8.dp),
                    ) {
                        CircularProgressIndicator()
                    }
                    OutlinedButton(
                        enabled = fieldsEnabled,
                        onClick = { performLogin() },
                    ) {
                        Text(stringResource(R.string.login_action))
                    }
                }
            }
        }
    }
}
