package uk.ac.cardiff.trainerhub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.ac.cardiff.trainerhub.domain.ClientSortMode

private val Context.dataStore by preferencesDataStore(name = "trainer_hub_preferences")

class AppPreferencesRepository(
    private val context: Context,
) {
    private object Keys {
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
        val clientSortMode = stringPreferencesKey("client_sort_mode")
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.remindersEnabled] ?: true }

    val clientSortMode: Flow<ClientSortMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.clientSortMode]?.let { runCatching { ClientSortMode.valueOf(it) }.getOrNull() }
            ?: ClientSortMode.NEXT_SESSION
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.remindersEnabled] = enabled }
    }

    suspend fun setClientSortMode(mode: ClientSortMode) {
        context.dataStore.edit { prefs -> prefs[Keys.clientSortMode] = mode.name }
    }
}
