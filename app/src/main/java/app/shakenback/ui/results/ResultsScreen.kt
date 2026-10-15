package app.shakenback.ui.results

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.shakenback.core.LevelChange
import app.shakenback.core.LevelOutcome
import app.shakenback.core.LevelRules
import app.shakenback.core.ModalityScore
import app.shakenback.core.SessionResult
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(
    result: SessionResult,
    outcome: LevelOutcome,
    haptics: Boolean,
    shakeHint: Boolean,
    onPlayAgain: () -> Unit,
    onPlayAtLevel: (Int) -> Unit,
    onHome: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (outcome.change == LevelChange.UP) {
                FistBump(haptics = haptics)
            } else {
                Text(
                    text = if (outcome.change == LevelChange.DOWN) "Stepping back a level" else "Session complete",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            ScoreRing(score = result.overall)
            Text(
                text = outcomeDetail(result, outcome),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            ModalityCard("Position", result.position)
            ModalityCard("Sound", result.audio)
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onPlayAgain,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
            ) {
                Text("Play ${outcome.level}-back", style = MaterialTheme.typography.titleMedium)
            }
            // Levelling up is a suggestion: let the player stay where they were.
            if (outcome.change == LevelChange.UP) {
                OutlinedButton(
                    onClick = { onPlayAtLevel(result.n) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text("Stay at ${result.n}-back")
                }
            }
            OutlinedButton(
                onClick = onHome,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Home")
            }
            if (shakeHint) {
                Text(
                    text = "Shake to go again",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun outcomeDetail(result: SessionResult, outcome: LevelOutcome): String = when (outcome.change) {
    LevelChange.UP -> "Level up! You cleared ${result.n}-back. Next up: ${outcome.level}-back."
    LevelChange.DOWN -> "Three low scores at ${result.n}-back. Rebuild at ${outcome.level}-back."
    LevelChange.SAME -> if (outcome.strikes > 0) {
        "${outcome.strikes} of ${LevelRules.STRIKES_TO_FALLBACK} low scores at ${result.n}-back."
    } else {
        "Reach ${pct(LevelRules.ADVANCE_THRESHOLD)} to move up from ${result.n}-back."
    }
}

@Composable
private fun ScoreRing(score: Double) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(score) {
        sweep.animateTo(score.toFloat(), tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(Modifier.size(196.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(inset, inset)
            drawArc(track, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            drawArc(
                brush = Brush.sweepGradient(listOf(primary, tertiary, secondary, primary)),
                startAngle = -90f,
                sweepAngle = 360f * sweep.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(pct(sweep.value.toDouble()), style = MaterialTheme.typography.displayMedium)
            Text(
                text = "overall",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ModalityCard(label: String, score: ModalityScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(pct(score.accuracy), style = MaterialTheme.typography.titleMedium)
            }
            LinearProgressIndicator(
                progress = { score.accuracy.toFloat() },
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
            Row {
                Stat("Hits", score.hits, Modifier.weight(1f))
                Stat("Missed", score.misses, Modifier.weight(1f))
                Stat("False alarms", score.falseAlarms, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun pct(value: Double) = "${(value * 100).roundToInt()}%"
