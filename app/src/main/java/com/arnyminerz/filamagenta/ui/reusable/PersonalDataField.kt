package com.arnyminerz.filamagenta.ui.reusable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Creates an [OutlinedTextField] with a label.
 * @author Arnau Mora
 * @since 20221016
 * @param value The current value of the field.
 * @param label The text resource of the text to display as label.
 * @param modifier Modifiers to apply to the composable. Note: [Modifier.fillMaxWidth] and
 * [Modifier.padding] are set automatically.
 * @param supportingTextRes If not null, will display this text in the bottom of
 * the text field.
 * @param onValueChange If null, the field will be readonly, otherwise will get called when the
 * field's value is changed.
 * @param isDisabled If true, the field will be disabled.
 * @param trailing Some composable to display at the end of the field.
 */
@Composable
@ExperimentalMaterial3Api
fun LabeledTextField(
    value: String?,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    @StringRes supportingTextRes: Int? = null,
    onValueChange: ((text: String) -> Unit)? = null,
    isDisabled: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
) {
    OutlinedTextField(
        value = value ?: "",
        onValueChange = onValueChange ?: {},
        enabled = !isDisabled,
        readOnly = onValueChange == null,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        label = { Text(stringResource(label)) },
        supportingText = supportingTextRes?.let { { Text(stringResource(supportingTextRes)) } },
        trailingIcon = trailing,
        colors = colors,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}

private val DateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@Composable
@ExperimentalMaterial3Api
fun LabeledTextField(
    value: Date?,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    @StringRes supportingTextRes: Int? = null,
    onValueChange: ((text: String) -> Unit)? = null,
    isDisabled: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
) = LabeledTextField(
    modifier = modifier,
    value = value?.let { DateFormatter.format(it) } ?: "----/--/--",
    label = label,
    supportingTextRes = supportingTextRes,
    onValueChange = onValueChange,
    isDisabled = isDisabled,
    trailing = trailing,
    colors = colors,
)

@Composable
@ExperimentalMaterial3Api
fun LabeledTextField(
    @StringRes localizedValue: Int,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    @StringRes supportingTextRes: Int? = null,
    onValueChange: ((text: String) -> Unit)? = null,
    isDisabled: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
) = LabeledTextField(
    modifier = modifier,
    value = stringResource(localizedValue),
    label = label,
    supportingTextRes = supportingTextRes,
    onValueChange = onValueChange,
    isDisabled = isDisabled,
    trailing = trailing,
    colors = colors,
)

@Preview(
    showBackground = true,
)
@Composable
@ExperimentalMaterial3Api
fun LabeledTextFieldPreview() {
    LabeledTextField(value = "The value", label = R.string.placeholder)
}
