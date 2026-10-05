package com.example.mad_assignment.model.other

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/* Add an extension to the Context called dataStore, the dataStore will create a file called user_preferences */
/* then when someone calling the dataStore, it is able to find the file "user_preferences" */
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
    }

    /* use to create and tracking the data inside the dataStore */
    /* if no create, set it to false */
    val darkModeFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_KEY]
                ?: false
        }

    val sortOrderFlow: Flow<SortOrder> = dataStore.data
        .map { preferences ->
            SortOrder.entries.firstOrNull { it.name == preferences[SORT_ORDER_KEY] }
                ?: SortOrder.TITLE
        }

    /* use this function to update the data of preferences */
    suspend fun saveDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDarkMode
        }
    }

    suspend fun saveSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.name
        }
    }
}