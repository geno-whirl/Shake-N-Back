package app.shakenback.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.shakenback.core.LevelRules
import app.shakenback.data.Settings
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onChange: ((Settings) -> Settings) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SettingsCard {
                    LevelStepper(
                        level = settings.level,
                        onLevelChange = { level -> onChange { it.copy(level = level, strikes = 0) } },
                    )
                    HorizontalDivider()
                    TrialSpeed(
                        millis = settings.trialMillis,
                        onChange = { millis -> onChange { it.copy(trialMillis = millis) } },
                    )
                }
                SettingsCard {
                    SwitchRow(
                        title = "Automatic levelling",
                        subtitle = "Up at ${(LevelRules.ADVANCE_THRESHOLD * 100).roundToInt()}%, " +
                            "down after ${LevelRules.STRIKES_TO_FALLBACK} sessions below " +
                            "${(LevelRules.FALLBACK_THRESHOLD * 100).roundToInt()}%",
                        checked = settings.autoProgress,
                        onCheckedChange = { value -> onChange { it.copy(autoProgress = value) } },
                    )
                    HorizontalDivider()
                    SwitchRow(
                        title = "Answer feedback",
                        subtitle = "Flash buttons green or red as you answer",
                        checked = settings.showFeedback,
                        onCheckedChange = { value -> onChange { it.copy(showFeedback = value) } },
                    )
                    HorizontalDivider()
                    SwitchRow(
                        title = "Haptics",
                        subtitle = "Vibrate lightly on each tap",
                        checked = settings.haptics,
                        onCheckedChange = { value -> onChange { it.copy(haptics = value) } },
                    )
                }
                TextButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Reset progress", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("This clears your level and session history. Settings are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onResetProgress()
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { content() }
    }
}

@Composable
private fun LevelStepper(level: Int, onLevelChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Level", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Set your N manually",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FilledTonalIconButton(
            onClick = { onLevelChange(level - 1) },
            enabled = level > LevelRules.MIN_LEVEL,
        ) { Text("−", style = MaterialTheme.typography.titleLarge) }
        Text(
            text = "$level",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        FilledTonalIconButton(
            onClick = { onLevelChange(level + 1) },
            enabled = level < LevelRules.MAX_LEVEL,
        ) { Text("+", style = MaterialTheme.typography.titleLarge) }
    }
}

@Composable
private fun TrialSpeed(millis: Long, onChange: (Long) -> Unit) {
    // Local state keeps the slider smooth; persist only when the drag ends.
    var seconds by remember(millis) { mutableFloatStateOf(millis / 1000f) }
    Column(Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Time per turn", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                text = String.format(Locale.US, "%.1f s", seconds),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = seconds,
            onValueChange = { seconds = it },
            onValueChangeFinished = { onChange((seconds * 1000).roundToLong()) },
            valueRange = Settings.MIN_TRIAL_MILLIS / 1000f..Settings.MAX_TRIAL_MILLIS / 1000f,
            steps = 3,
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(end = 16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
