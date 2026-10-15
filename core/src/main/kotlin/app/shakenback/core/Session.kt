package app.shakenback.core

/** A single play-through: the stimulus sequence plus the player's responses. */
class Session(val config: SessionConfig, val stimuli: List<Stimulus>) {

    init {
        require(stimuli.size == config.trialCount) {
            "Expected ${config.trialCount} stimuli, got ${stimuli.size}"
        }
    }

    private val responses: Map<Modality, MutableSet<Int>> =
        Modality.entries.associateWith { mutableSetOf() }

    val n: Int get() = config.n

    fun isScored(trial: Int): Boolean = trial >= n && trial < stimuli.size

    fun isMatch(trial: Int, modality: Modality): Boolean =
        isScored(trial) &&
            stimuli[trial].valueOf(modality) == stimuli[trial - n].valueOf(modality)

    fun hasResponded(trial: Int, modality: Modality): Boolean =
        trial in responses.getValue(modality)

    /**
     * Records a "match" response. Returns whether it was correct, or null when
     * the response is ignored (warm-up trial or duplicate press).
     */
    fun respond(trial: Int, modality: Modality): Boolean? {
        if (!isScored(trial)) return null
        if (!responses.getValue(modality).add(trial)) return null
        return isMatch(trial, modality)
    }

    fun result(): SessionResult =
        SessionResult(n = n, position = score(Modality.POSITION), audio = score(Modality.AUDIO))

    private fun score(modality: Modality): ModalityScore {
        var hits = 0
        var misses = 0
        var falseAlarms = 0
        var correctRejections = 0
        for (trial in n until stimuli.size) {
            val match = isMatch(trial, modality)
            val pressed = hasResponded(trial, modality)
            when {
                match && pressed -> hits++
                match -> misses++
                pressed -> falseAlarms++
                else -> correctRejections++
            }
        }
        return ModalityScore(hits, misses, falseAlarms, correctRejections)
    }
}
