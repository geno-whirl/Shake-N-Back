package app.shakenback.core

import kotlin.random.Random

/**
 * Builds a stimulus sequence with exactly [SessionConfig.matchesPerModality]
 * targets per modality. Non-target trials are guaranteed to differ from the
 * value n back, so no accidental extra targets appear.
 */
class SequenceGenerator(private val random: Random = Random.Default) {

    fun generate(config: SessionConfig): List<Stimulus> {
        val n = config.n
        val scored = (n until config.trialCount).toList()
        val positionTargets = scored.shuffled(random).take(config.matchesPerModality).toSet()
        val audioTargets = scored.shuffled(random).take(config.matchesPerModality).toSet()

        val positions = ArrayList<Int>(config.trialCount)
        val letters = ArrayList<Char>(config.trialCount)
        for (i in 0 until config.trialCount) {
            positions += pick(i, n, positions, i in positionTargets, Stimuli.POSITIONS)
            letters += pick(i, n, letters, i in audioTargets, Stimuli.LETTERS)
        }
        return positions.zip(letters) { p, l -> Stimulus(p, l) }
    }

    private fun <T> pick(i: Int, n: Int, previous: List<T>, isTarget: Boolean, pool: List<T>): T {
        if (i < n) return pool.random(random)
        val nBack = previous[i - n]
        return if (isTarget) nBack else pool.filter { it != nBack }.random(random)
    }
}
