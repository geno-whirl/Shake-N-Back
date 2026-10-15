package app.shakenback.core

import org.junit.Assert.assertEquals
import org.junit.Test

class LevelRulesTest {

    @Test
    fun `advances at the threshold`() {
        assertEquals(LevelOutcome(3, 0, LevelChange.UP), LevelRules.next(2, 2, 0.80))
    }

    @Test
    fun `holds between thresholds and keeps strikes`() {
        assertEquals(LevelOutcome(2, 1, LevelChange.SAME), LevelRules.next(2, 1, 0.65))
    }

    @Test
    fun `drops after three poor sessions`() {
        var outcome = LevelOutcome(3, 0, LevelChange.SAME)
        repeat(2) {
            outcome = LevelRules.next(outcome.level, outcome.strikes, 0.3)
            assertEquals(3, outcome.level)
        }
        outcome = LevelRules.next(outcome.level, outcome.strikes, 0.3)
        assertEquals(LevelOutcome(2, 0, LevelChange.DOWN), outcome)
    }

    @Test
    fun `respects level bounds`() {
        assertEquals(LevelChange.SAME, LevelRules.next(LevelRules.MAX_LEVEL, 0, 1.0).change)
        assertEquals(
            LevelOutcome(LevelRules.MIN_LEVEL, LevelRules.STRIKES_TO_FALLBACK, LevelChange.SAME),
            LevelRules.next(LevelRules.MIN_LEVEL, 2, 0.0),
        )
    }
}
