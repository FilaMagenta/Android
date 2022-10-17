package com.arnyminerz.filamagenta.ui.pages

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.arnyminerz.filamagenta.BuildConfig
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.ui.reusable.ListDialogOptions
import com.arnyminerz.filamagenta.ui.reusable.SettingsCategory
import com.arnyminerz.filamagenta.ui.reusable.SettingsDataDialog
import com.arnyminerz.filamagenta.ui.reusable.SettingsItem
import java.util.Locale

@Composable
@ExperimentalMaterial3Api
fun SettingsPage() {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxSize(),
    ) {
        SettingsCategory(stringResource(R.string.settings_category_general))

        val locale = AppCompatDelegate.getApplicationLocales()
        SettingsItem(
            title = stringResource(R.string.settings_language_title),
            subtitle = stringResource(R.string.settings_language_summary),
            stateString = locale.get(0)?.toString() ?: "",
            dialog = SettingsDataDialog(
                title = stringResource(R.string.settings_language_dialog_title),
                list = ListDialogOptions(
                    items = BuildConfig.TRANSLATION_ARRAY.associateWith { Locale.forLanguageTag(it).displayLanguage },
                ),
            ),
            setString = { lang ->
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
                AppCompatDelegate.setApplicationLocales(appLocale)
            },
        )
    }
}
