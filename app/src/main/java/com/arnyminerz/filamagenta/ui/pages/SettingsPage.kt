package com.arnyminerz.filamagenta.ui.pages

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnyminerz.filamagenta.BuildConfig
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.data.ANALYTICS_COLLECTION
import com.arnyminerz.filamagenta.data.ERROR_COLLECTION
import com.arnyminerz.filamagenta.ui.reusable.ListDialogOptions
import com.arnyminerz.filamagenta.ui.reusable.SettingsCategory
import com.arnyminerz.filamagenta.ui.reusable.SettingsDataDialog
import com.arnyminerz.filamagenta.ui.reusable.SettingsItem
import com.arnyminerz.filamagenta.ui.viewmodel.SimpleViewModel
import com.arnyminerz.filamagenta.utils.dataStore
import com.arnyminerz.filamagenta.utils.getBooleanPreferences
import java.util.Locale

@Composable
@ExperimentalMaterial3Api
fun SettingsPage(
    viewModel: SimpleViewModel = viewModel(),
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxSize(),
    ) {
        val locale = AppCompatDelegate.getApplicationLocales()
        val collectErrors by context.getBooleanPreferences(ERROR_COLLECTION, true)
            .collectAsState(null)
        val collectAnalytics by context.getBooleanPreferences(ANALYTICS_COLLECTION, true)
            .collectAsState(null)

        var collectErrorsEnabled by remember { mutableStateOf(true) }
        var collectAnalyticsEnabled by remember { mutableStateOf(true) }

        SettingsCategory(stringResource(R.string.settings_category_general))
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

        SettingsCategory(stringResource(R.string.settings_category_advanced))
        SettingsItem(
            title = stringResource(R.string.settings_collect_error_title),
            subtitle = stringResource(R.string.settings_collect_error_summary),
            stateBoolean = collectErrors,
            switch = true,
            enabled = collectErrorsEnabled,
            setBoolean = { enabled ->
                collectErrorsEnabled = false

                viewModel.invoke {
                    context.dataStore.edit { it[ERROR_COLLECTION] = enabled }
                }.invokeOnCompletion {
                    collectErrorsEnabled = true
                }
            },
        )
        SettingsItem(
            title = stringResource(R.string.settings_collect_analytics_title),
            subtitle = stringResource(R.string.settings_collect_analytics_summary),
            stateBoolean = collectAnalytics,
            switch = true,
            enabled = collectAnalyticsEnabled,
            setBoolean = { enabled ->
                collectAnalyticsEnabled = false

                viewModel.invoke {
                    context.dataStore.edit { it[ANALYTICS_COLLECTION] = enabled }
                }.invokeOnCompletion {
                    collectAnalyticsEnabled = true
                }
            },
        )
    }
}
