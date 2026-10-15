package app.shakenback.data

import app.shakenback.core.SessionResult
import kotlinx.serialization.Serializable

data class Settings(
    val level: Int = 2,
    /** Sessions below 50% at the current level; three trigger a fallback (see LevelRules). */
    val strikes: Int = 0,
    val trialMillis: Long = DEFAULT_TRIAL_MILLIS,
    val autoProgress: Boolean = true,
    val showFeedback: Boolean = true,
    val haptics: Boolean = true,
) {
    companion object {
        const val DEFAULT_TRIAL_MILLIS = 3_000L
        const val MIN_TRIAL_MILLIS = 2_000L
        const val MAX_TRIAL_MILLIS = 4_000L
    }
}

@Serializable
data class SessionRecord(
    val timestamp: Long,
    val n: Int,
    val position: Double,
    val audio: Double,
    val overall: Double,
) {
    companion object {
        fun from(result: SessionResult, timestamp: Long) = SessionRecord(
            timestamp = timestamp,
            n = result.n,
            position = result.position.accuracy,
            audio = result.audio.accuracy,
            overall = result.overall,
        )
    }
}
