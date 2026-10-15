package app.shakenback.ui.results

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Level-up celebration: two fists bump in the middle with a burst, then the
 * app's namesake catchphrase pops in. A nod to Talladega Nights' "Shake and bake!".
 */
@Composable
fun FistBump(haptics: Boolean, modifier: Modifier = Modifier) {
    val view = LocalView.current
    val approach = remember { Animatable(0f) } // 0 = apart, 1 = knuckles touching
    val burst = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        approach.animateTo(1f, tween(durationMillis = 360, easing = FastOutLinearInEasing))
        if (haptics) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        launch {
            approach.animateTo(0.88f, spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMedium))
        }
        launch { burst.animateTo(1f, tween(durationMillis = 550, easing = LinearOutSlowInEasing)) }
        title.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
    }

    val colors = MaterialTheme.colorScheme
    val sparkColors = listOf(colors.primary, colors.tertiary, colors.secondary)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(150.dp)) {
                val progress = burst.value
                if (progress <= 0f || progress >= 1f) return@Canvas
                val alpha = 1f - progress
                val radius = size.minDimension / 2 * progress
                drawCircle(colors.tertiary.copy(alpha = alpha), radius = radius, style = Stroke(width = 3.dp.toPx()))
                repeat(SPARKS) { i ->
                    val angle = (2 * PI * i / SPARKS).toFloat()
                    val direction = Offset(cos(angle), sin(angle))
                    drawLine(
                        color = sparkColors[i % sparkColors.size].copy(alpha = alpha),
                        start = center + direction * (radius * 0.55f),
                        end = center + direction * (radius * 0.95f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            val gap = lerp(130.dp, 34.dp, approach.value)
            Text("🤜", fontSize = 60.sp, modifier = Modifier.offset(x = -gap))
            Text("🤛", fontSize = 60.sp, modifier = Modifier.offset(x = gap))
        }
        Text(
            text = "Shake-N-Back!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.graphicsLayer {
                scaleX = title.value
                scaleY = title.value
                alpha = title.value.coerceIn(0f, 1f)
            },
        )
    }
}

private const val SPARKS = 8
