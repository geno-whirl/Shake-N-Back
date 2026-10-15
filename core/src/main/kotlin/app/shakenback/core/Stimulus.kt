package app.shakenback.core

enum class Modality { POSITION, AUDIO }

/**
 * One trial's stimulus: a highlighted grid cell plus a spoken letter.
 *
 * [position] indexes the 3x3 grid row-major (0..8). The centre cell (4) is never
 * used, matching Brain Workshop, so it can hold a fixation point.
 */
data class Stimulus(val position: Int, val letter: Char) {
    fun valueOf(modality: Modality): Any = when (modality) {
        Modality.POSITION -> position
        Modality.AUDIO -> letter
    }
}

object Stimuli {
    const val CENTER_CELL = 4
    val POSITIONS: List<Int> = (0..8).filter { it != CENTER_CELL }

    /** Acoustically distinct letters, the same set Brain Workshop uses. */
    val LETTERS: List<Char> = listOf('C', 'H', 'K', 'L', 'Q', 'R', 'S', 'T')
}
