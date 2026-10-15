package app.shakenback

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.shakenback.audio.LetterSpeaker
import app.shakenback.core.LevelChange
import app.shakenback.core.LevelOutcome
import app.shakenback.core.LevelRules
import app.shakenback.core.Modality
import app.shakenback.core.SequenceGenerator
import app.shakenback.core.Session
import app.shakenback.core.SessionConfig
import app.shakenback.core.SessionResult
import app.shakenback.data.ProgressRepository
import app.shakenback.data.SessionRecord
import app.shakenback.data.Settings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data object Game : Screen
    data class Results(val result: SessionResult, val outcome: LevelOutcome) : Screen
    data object Settings : Screen
}

enum class ButtonFeedback { IDLE, PRESSED, CORRECT, WRONG }

data class GameUiState(
    val n: Int = 2,
    val trial: Int = 0,
    val trialCount: Int = 0,
    /** Grid cell currently lit, or null between stimuli. */
    val activeCell: Int? = null,
    /** Pre-session countdown value, or null once trials are running. */
    val countdown: Int? = null,
    val positionFeedback: ButtonFeedback = ButtonFeedback.IDLE,
    val audioFeedback: ButtonFeedback = ButtonFeedback.IDLE,
) {
    val acceptingResponses: Boolean get() = countdown == null && trial >= n
    val inWarmUp: Boolean get() = countdown == null && trial < n

    fun feedbackFor(modality: Modality): ButtonFeedback = when (modality) {
        Modality.POSITION -> positionFeedback
        Modality.AUDIO -> audioFeedback
    }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProgressRepository(application)
    private val speaker = LetterSpeaker(application)
    private val generator = SequenceGenerator()

    val settings: StateFlow<Settings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())
    val history: StateFlow<List<SessionRecord>> =
        repository.history.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val speechReady: StateFlow<Boolean?> = speaker.ready
    val shakeAvailable: Boolean = application.getSystemService(SensorManager::class.java)
        ?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    private val _screen = MutableStateFlow<Screen>(Screen.Home)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _game = MutableStateFlow(GameUiState())
    val game: StateFlow<GameUiState> = _game.asStateFlow()

    private var session: Session? = null
    private var sessionJob: Job? = null

    fun navigate(screen: Screen) {
        _screen.value = screen
    }

    fun startSession() {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            val current = repository.settings.first()
            val config = SessionConfig(n = current.level)
            val active = Session(config, generator.generate(config))
            session = active
            _game.value = GameUiState(
                n = config.n,
                trialCount = config.trialCount,
                countdown = COUNTDOWN_FROM,
            )
            _screen.value = Screen.Game

            for (count in COUNTDOWN_FROM downTo 1) {
                _game.update { it.copy(countdown = count) }
                delay(COUNTDOWN_STEP_MILLIS)
            }
            active.stimuli.forEachIndexed { trial, stimulus ->
                _game.update {
                    it.copy(
                        countdown = null,
                        trial = trial,
                        activeCell = stimulus.position,
                        positionFeedback = ButtonFeedback.IDLE,
                        audioFeedback = ButtonFeedback.IDLE,
                    )
                }
                speaker.speak(stimulus.letter)
                delay(STIMULUS_MILLIS)
                _game.update { it.copy(activeCell = null) }
                delay(current.trialMillis - STIMULUS_MILLIS)
            }
            finish(active, current)
        }
    }

    /**
     * Shake starts a session from Home or Results, and restarts a running one.
     * Returns whether the shake did anything.
     */
    fun onShake(): Boolean = when (_screen.value) {
        Screen.Home, Screen.Game, is Screen.Results -> {
            speaker.stop()
            startSession()
            true
        }
        Screen.Settings -> false
    }

    fun respond(modality: Modality) {
        val active = session ?: return
        val state = _game.value
        if (!state.acceptingResponses) return
        val correct = active.respond(state.trial, modality) ?: return
        val feedback = when {
            !settings.value.showFeedback -> ButtonFeedback.PRESSED
            correct -> ButtonFeedback.CORRECT
            else -> ButtonFeedback.WRONG
        }
        _game.update {
            when (modality) {
                Modality.POSITION -> it.copy(positionFeedback = feedback)
                Modality.AUDIO -> it.copy(audioFeedback = feedback)
            }
        }
    }

    /** Abandons the running session without recording it. */
    fun abortSession() {
        sessionJob?.cancel()
        sessionJob = null
        session = null
        speaker.stop()
        _screen.value = Screen.Home
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        viewModelScope.launch { repository.updateSettings(transform) }
    }

    /** Manual level change; clears strikes since they count toward the old level. */
    fun setLevel(level: Int) {
        updateSettings {
            it.copy(level = level.coerceIn(LevelRules.MIN_LEVEL, LevelRules.MAX_LEVEL), strikes = 0)
        }
    }

    fun setAutoProgress(enabled: Boolean) {
        updateSettings { it.copy(autoProgress = enabled) }
    }

    /** Sets the level, then starts a session at it once the change is saved. */
    fun playAtLevel(level: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(level = level, strikes = 0) }
            startSession()
        }
    }

    fun resetProgress() {
        viewModelScope.launch { repository.resetProgress() }
    }

    private suspend fun finish(active: Session, settings: Settings) {
        val result = active.result()
        val outcome = if (settings.autoProgress) {
            LevelRules.next(settings.level, settings.strikes, result.overall)
        } else {
            LevelOutcome(settings.level, settings.strikes, LevelChange.SAME)
        }
        repository.recordSession(SessionRecord.from(result, System.currentTimeMillis()), outcome)
        session = null
        _screen.value = Screen.Results(result, outcome)
    }

    override fun onCleared() {
        speaker.shutdown()
    }

    private companion object {
        const val COUNTDOWN_FROM = 3
        const val COUNTDOWN_STEP_MILLIS = 700L
        const val STIMULUS_MILLIS = 500L
    }
}
