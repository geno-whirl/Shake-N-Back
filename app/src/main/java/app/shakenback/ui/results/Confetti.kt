package app.shakenback.ui.results

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.sin
import kotlin.random.Random

private class Piece(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val sway: Float,
    val spin: Float,
    val colorIndex: Int,
)

/** One shower of racing-coloured confetti. Draw-only, so it never blocks taps. */
@Composable
fun Confetti(modifier: Modifier = Modifier, pieces: Int = 70) {
    val colors = MaterialTheme.colorScheme
    val palette = listOf(colors.primary, colors.tertiary, colors.secondary, Color.White)
    val confetti = remember {
        val random = Random(System.nanoTime())
        List(pieces) {
            Piece(
                x = random.nextFloat(),
                delay = random.nextFloat() * 0.35f,
                speed = 0.8f + random.nextFloat() * 0.6f,
                sway = 10f + random.nextFloat() * 24f,
                spin = 360f + random.nextFloat() * 720f,
                colorIndex = random.nextInt(4),
            )
        }
    }
    val time = remember { Animatable(0f) }
    LaunchedEffect(Unit) { time.animateTo(1f, tween(durationMillis = 2_800, easing = LinearEasing)) }

    Canvas(modifier) {
        val t = time.value
        if (t >= 1f) return@Canvas
        val pieceSize = Size(8.dp.toPx(), 14.dp.toPx())
        confetti.forEach { piece ->
            val progress = ((t - piece.delay) / (1f - piece.delay)).coerceIn(0f, 1f) * piece.speed
            if (progress <= 0f) return@forEach
            val y = -pieceSize.height + progress * (size.height + pieceSize.height * 2)
            val x = piece.x * size.width + sin(progress * 12f) * piece.sway.dp.toPx() / 2
            val alpha = (1f - t).coerceIn(0f, 1f) * 2f
            translate(x, y) {
                rotate(piece.spin * progress, pivot = Offset(pieceSize.width / 2, pieceSize.height / 2)) {
                    drawRect(
                        color = palette[piece.colorIndex].copy(alpha = alpha.coerceAtMost(1f)),
                        size = pieceSize,
                    )
                }
            }
        }
    }
}
