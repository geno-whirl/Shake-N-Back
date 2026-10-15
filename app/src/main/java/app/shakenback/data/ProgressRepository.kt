package app.shakenback.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.shakenback.core.LevelOutcome
import app.shakenback.core.LevelRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

/** Settings, level progress and session history, all stored locally in DataStore. */
class ProgressRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    val settings: Flow<Settings> = dataStore.data.map(::readSettings)

    val history: Flow<List<SessionRecord>> = dataStore.data.map { decodeHistory(it[Keys.HISTORY]) }

    suspend fun updateSettings(transform: (Settings) -> Settings) {
        dataStore.edit { prefs -> writeSettings(prefs, transform(readSettings(prefs))) }
    }

    suspend fun recordSession(record: SessionRecord, outcome: LevelOutcome) {
        dataStore.edit { prefs ->
            prefs[Keys.LEVEL] = outcome.level
            prefs[Keys.STRIKES] = outcome.strikes
            val history = (decodeHistory(prefs[Keys.HISTORY]) + record).takeLast(MAX_HISTORY)
            prefs[Keys.HISTORY] = json.encodeToString(history)
        }
    }

    suspend fun resetProgress() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.LEVEL)
            prefs.remove(Keys.STRIKES)
            prefs.remove(Keys.HISTORY)
        }
    }

    private fun readSettings(prefs: Preferences): Settings {
        val defaults = Settings()
        return Settings(
            level = (prefs[Keys.LEVEL] ?: defaults.level)
                .coerceIn(LevelRules.MIN_LEVEL, LevelRules.MAX_LEVEL),
            strikes = prefs[Keys.STRIKES] ?: defaults.strikes,
            trialMillis = (prefs[Keys.TRIAL_MILLIS] ?: defaults.trialMillis)
                .coerceIn(Settings.MIN_TRIAL_MILLIS, Settings.MAX_TRIAL_MILLIS),
            autoProgress = prefs[Keys.AUTO_PROGRESS] ?: defaults.autoProgress,
            showFeedback = prefs[Keys.SHOW_FEEDBACK] ?: defaults.showFeedback,
            haptics = prefs[Keys.HAPTICS] ?: defaults.haptics,
            shakeControls = prefs[Keys.SHAKE_CONTROLS] ?: defaults.shakeControls,
        )
    }

    private fun writeSettings(prefs: MutablePreferences, settings: Settings) {
        prefs[Keys.LEVEL] = settings.level
        prefs[Keys.STRIKES] = settings.strikes
        prefs[Keys.TRIAL_MILLIS] = settings.trialMillis
        prefs[Keys.AUTO_PROGRESS] = settings.autoProgress
        prefs[Keys.SHOW_FEEDBACK] = settings.showFeedback
        prefs[Keys.HAPTICS] = settings.haptics
        prefs[Keys.SHAKE_CONTROLS] = settings.shakeControls
    }

    private fun decodeHistory(raw: String?): List<SessionRecord> =
        raw?.let { runCatching { json.decodeFromString<List<SessionRecord>>(it) }.getOrNull() }
            .orEmpty()

    private object Keys {
        val LEVEL = intPreferencesKey("level")
        val STRIKES = intPreferencesKey("strikes")
        val TRIAL_MILLIS = longPreferencesKey("trial_millis")
        val AUTO_PROGRESS = booleanPreferencesKey("auto_progress")
        val SHOW_FEEDBACK = booleanPreferencesKey("show_feedback")
        val HAPTICS = booleanPreferencesKey("haptics")
        val SHAKE_CONTROLS = booleanPreferencesKey("shake_controls")
        val HISTORY = stringPreferencesKey("history")
    }

    private companion object {
        const val MAX_HISTORY = 1_000
    }
}
