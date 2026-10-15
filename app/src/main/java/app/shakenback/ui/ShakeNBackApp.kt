package app.shakenback.ui

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.shakenback.AppViewModel
import app.shakenback.Screen
import app.shakenback.ui.game.GameScreen
import app.shakenback.ui.home.HomeScreen
import app.shakenback.ui.results.ResultsScreen
import app.shakenback.ui.settings.SettingsScreen

@Composable
fun ShakeNBackApp(viewModel: AppViewModel = viewModel()) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val speechReady by viewModel.speechReady.collectAsStateWithLifecycle()

    val view = LocalView.current
    val shakeEnabled = settings.shakeControls && viewModel.shakeAvailable

    ShakeEffect(enabled = shakeEnabled) {
        if (viewModel.onShake() && settings.haptics) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    BackHandler(enabled = screen != Screen.Home) {
        if (screen == Screen.Game) viewModel.abortSession() else viewModel.navigate(Screen.Home)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
            contentKey = { it::class },
            label = "screen",
        ) { current ->
            when (current) {
                Screen.Home -> HomeScreen(
                    settings = settings,
                    history = history,
                    speechReady = speechReady,
                    shakeHint = shakeEnabled,
                    onStart = viewModel::startSession,
                    onOpenSettings = { viewModel.navigate(Screen.Settings) },
                )
                Screen.Game -> {
                    val game by viewModel.game.collectAsStateWithLifecycle()
                    GameScreen(
                        state = game,
                        haptics = settings.haptics,
                        shakeHint = shakeEnabled,
                        onRespond = viewModel::respond,
                        onQuit = viewModel::abortSession,
                    )
                }
                is Screen.Results -> ResultsScreen(
                    result = current.result,
                    outcome = current.outcome,
                    haptics = settings.haptics,
                    shakeHint = shakeEnabled,
                    onPlayAgain = viewModel::startSession,
                    onHome = { viewModel.navigate(Screen.Home) },
                )
                Screen.Settings -> SettingsScreen(
                    settings = settings,
                    onChange = viewModel::updateSettings,
                    onResetProgress = viewModel::resetProgress,
                    onBack = { viewModel.navigate(Screen.Home) },
                )
            }
        }
    }
}
