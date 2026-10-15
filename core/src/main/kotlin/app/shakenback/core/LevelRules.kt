package app.shakenback.core

enum class LevelChange { UP, SAME, DOWN }

data class LevelOutcome(val level: Int, val strikes: Int, val change: LevelChange)

/**
 * Brain Workshop's progression: advance at >= 80%, and drop a level after
 * three sessions below 50% at the same level. Strikes reset on any level change.
 */
object LevelRules {
    const val ADVANCE_THRESHOLD = 0.80
    const val FALLBACK_THRESHOLD = 0.50
    const val STRIKES_TO_FALLBACK = 3
    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 9

    fun next(level: Int, strikes: Int, overall: Double): LevelOutcome = when {
        overall >= ADVANCE_THRESHOLD && level < MAX_LEVEL ->
            LevelOutcome(level + 1, 0, LevelChange.UP)
        overall < FALLBACK_THRESHOLD -> {
            val newStrikes = strikes + 1
            if (newStrikes >= STRIKES_TO_FALLBACK && level > MIN_LEVEL) {
                LevelOutcome(level - 1, 0, LevelChange.DOWN)
            } else {
                LevelOutcome(level, newStrikes.coerceAtMost(STRIKES_TO_FALLBACK), LevelChange.SAME)
            }
        }
        else -> LevelOutcome(level, strikes, LevelChange.SAME)
    }
}
