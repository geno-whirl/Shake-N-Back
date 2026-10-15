package app.shakenback.core

data class ModalityScore(
    val hits: Int,
    val misses: Int,
    val falseAlarms: Int,
    val correctRejections: Int,
) {
    /** Brain Workshop scoring: hits / (hits + misses + false alarms). */
    val accuracy: Double
        get() {
            val denominator = hits + misses + falseAlarms
            return if (denominator == 0) 1.0 else hits.toDouble() / denominator
        }
}

data class SessionResult(
    val n: Int,
    val position: ModalityScore,
    val audio: ModalityScore,
) {
    /** Pooled across modalities, so one weak modality can't be hidden by averaging. */
    val overall: Double
        get() {
            val hits = position.hits + audio.hits
            val denominator = hits + position.misses + audio.misses +
                position.falseAlarms + audio.falseAlarms
            return if (denominator == 0) 1.0 else hits.toDouble() / denominator
        }

    fun scoreFor(modality: Modality): ModalityScore = when (modality) {
        Modality.POSITION -> position
        Modality.AUDIO -> audio
    }
}
