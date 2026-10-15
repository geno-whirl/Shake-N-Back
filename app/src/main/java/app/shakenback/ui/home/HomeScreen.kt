package app.shakenback.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.shakenback.core.LevelRules
import app.shakenback.data.SessionRecord
import app.shakenback.data.Settings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    settings: Settings,
    history: List<SessionRecord>,
    speechReady: Boolean?,
    shakeHint: Boolean,
    onStart: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Shake-N-Back",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            LevelHero(settings)

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start session", style = MaterialTheme.typography.titleMedium)
            }
            if (shakeHint) {
                Text(
                    text = "…or give your phone a good shake",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (speechReady == false) SpeechWarning()

            StatsRow(history)
            ProgressCard(history)
            HowToPlayCard(settings.level)
        }
    }
}

@Composable
private fun LevelHero(settings: Settings) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.linearGradient(listOf(colors.primary, colors.tertiary)))
            .padding(24.dp),
    ) {
        Column {
            Text(
                text = "Dual N-Back · position + sound",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimary.copy(alpha = 0.8f),
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = settings.level.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.onPrimary,
                )
                Text(
                    text = "-back",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
                )
            }
            Text(
                text = if (settings.autoProgress) {
                    "Score ${pct(LevelRules.ADVANCE_THRESHOLD)} to level up"
                } else {
                    "Automatic levelling is off"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onPrimary.copy(alpha = 0.85f),
            )
            if (settings.autoProgress && settings.strikes > 0) {
                Spacer(Modifier.height(12.dp))
                StrikeDots(settings.strikes)
            }
        }
    }
}

@Composable
private fun StrikeDots(strikes: Int) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(LevelRules.STRIKES_TO_FALLBACK) { index ->
            Box(
                Modifier
                    .padding(end = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (index < strikes) onPrimary else onPrimary.copy(alpha = 0.3f)),
            )
        }
        Text(
            text = "$strikes of ${LevelRules.STRIKES_TO_FALLBACK} low scores at this level",
            style = MaterialTheme.typography.labelMedium,
            color = onPrimary.copy(alpha = 0.85f),
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun SpeechWarning() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Text-to-speech isn't available, so letters will be silent. " +
                    "Install an English voice in your system settings.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StatsRow(history: List<SessionRecord>) {
    val today = remember(history) {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.now(zone)
        history.count { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == date }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile("Today", today.toString(), Modifier.weight(1f))
        StatTile("Sessions", history.size.toString(), Modifier.weight(1f))
        StatTile("Peak", history.maxOfOrNull { it.n }?.let { "$it-back" } ?: "—", Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProgressCard(history: List<SessionRecord>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Progress", style = MaterialTheme.typography.titleMedium)
            val recent = history.takeLast(CHART_SESSIONS)
            if (recent.isEmpty()) {
                Text(
                    text = "Your level history will appear here after your first session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Text(
                    text = "Level over your last ${recent.size} sessions · " +
                        "last score ${pct(recent.last().overall)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                LevelChart(
                    records = recent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                )
            }
        }
    }
}

@Composable
private fun LevelChart(records: List<SessionRecord>, modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val maxLevel = maxOf(records.maxOf { it.n } + 1, 3)
    val minLevel = maxOf(records.minOf { it.n } - 1, LevelRules.MIN_LEVEL - 1)

    Canvas(modifier) {
        inset(8.dp.toPx()) {
            val range = (maxLevel - minLevel).toFloat()
            fun y(level: Int) = size.height - (level - minLevel) / range * size.height
            val stepX = if (records.size > 1) size.width / (records.size - 1) else 0f
            val points = records.mapIndexed { i, record -> Offset(i * stepX, y(record.n)) }

            for (level in minLevel..maxLevel) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y(level)),
                    end = Offset(size.width, y(level)),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val path = Path().apply {
                points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
            }
            val area = Path().apply {
                addPath(path)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(line.copy(alpha = 0.28f), Color.Transparent)))
            drawPath(
                path = path,
                color = line,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            points.forEach { drawCircle(line, radius = 4.dp.toPx(), center = it) }
        }
    }
}

@Composable
private fun HowToPlayCard(level: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("How to play", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Each turn a square lights up and a letter is spoken. Tap Position when " +
                    "the square is where it was $level ${if (level == 1) "turn" else "turns"} ago, " +
                    "and Sound when the letter is the same as $level back. " +
                    "Both can match at once.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun pct(value: Double) = "${(value * 100).roundToInt()}%"

private const val CHART_SESSIONS = 30
