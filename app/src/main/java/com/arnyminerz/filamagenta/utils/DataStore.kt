package com.arnyminerz.filamagenta.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.arnyminerz.filamagenta.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Storage for preferences.
 * @author Arnau Mora
 * @since 20221109
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = BuildConfig.APPLICATION_ID)

fun Context.getIntPreferences(key: Preferences.Key<Int>, default: Int = 0): Flow<Int> =
    dataStore
        .data
        .map { it[key] ?: default }
