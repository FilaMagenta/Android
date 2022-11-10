package com.arnyminerz.filamagenta.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

val ACCOUNT_INDEX = intPreferencesKey("account_index")

val LAST_EVENTS_SYNC = longPreferencesKey("events_sync_time")

val ERROR_COLLECTION = booleanPreferencesKey("error_collection")

val ANALYTICS_COLLECTION = booleanPreferencesKey("analytics_collection")
