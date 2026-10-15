package app.shakenback.core

data class SessionConfig(
    val n: Int,
    /** Brain Workshop default: 20 scored trials plus the n warm-up trials. */
    val trialCount: Int = 20 + n,
    /** Exact number of targets per modality among the scored trials. */
    val matchesPerModality: Int = 6,
) {
    init {
        require(n >= 1) { "n must be at least 1, was $n" }
        require(trialCount > n) { "trialCount ($trialCount) must exceed n ($n)" }
        require(matchesPerModality in 0..(trialCount - n)) {
            "matchesPerModality ($matchesPerModality) must fit in ${trialCount - n} scored trials"
        }
    }

    val scoredTrials: Int get() = trialCount - n
}
