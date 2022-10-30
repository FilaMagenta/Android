package com.arnyminerz.filamagenta.ui.reusable

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.ui.theme.InterFontFamily

@ExperimentalMaterial3Api
@Composable
fun LoginField(
    text: String,
    onTextChange: (text: String) -> Unit,
    enabled: Boolean,
    @StringRes label: Int,
    error: String?,
    onClearErrorRequested: () -> Unit,
    isPasswordField: Boolean = false,
    actionNext: Boolean = true,
    onActionPerformed: () -> Unit,
    focusRequester: FocusRequester? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { onTextChange(it) },
        enabled = enabled,
        label = { Text(stringResource(label)) },
        supportingText = {
            AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                Text(error ?: "")
            }
            AnimatedVisibility(visible = error == null, enter = fadeIn(), exit = fadeOut()) {
                Text("")
            }
        },
        trailingIcon = if (isPasswordField) {
            {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                ) {
                    if (passwordVisible)
                        Icon(
                            Icons.Rounded.Visibility,
                            stringResource(R.string.login_action_hide_password),
                        )
                    else
                        Icon(
                            Icons.Rounded.VisibilityOff,
                            stringResource(R.string.login_action_show_password),
                        )
                }
            }
        } else null,
        singleLine = true,
        isError = error != null,
        visualTransformation = if (!isPasswordField || passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            imeAction = if (actionNext) ImeAction.Next else ImeAction.Go,
            keyboardType = if (isPasswordField) KeyboardType.Password else KeyboardType.Text,
        ),
        keyboardActions = KeyboardActions(
            onGo = { onActionPerformed() },
            onNext = { onActionPerformed() },
        ),
        textStyle = LocalTextStyle.current.copy(
            fontFamily = InterFontFamily,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { onClearErrorRequested() },
    )
}
