package com.example.mad_assignment.Model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    }

    val darkModeFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_KEY] ?: false
        }

    suspend fun saveDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDarkMode
        }
    }
}