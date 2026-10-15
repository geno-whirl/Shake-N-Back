package app.shakenback.core

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SequenceGeneratorTest {

    @Test
    fun `generates exactly the configured number of targets per modality`() {
        for (seed in 0 until 200) {
            for (n in 1..5) {
                val config = SessionConfig(n = n)
                val session = Session(config, SequenceGenerator(Random(seed)).generate(config))
                for (modality in Modality.entries) {
                    val targets = (0 until config.trialCount).count { session.isMatch(it, modality) }
                    assertEquals("seed=$seed n=$n $modality", config.matchesPerModality, targets)
                }
            }
        }
    }

    @Test
    fun `uses only valid positions and letters`() {
        val config = SessionConfig(n = 2)
        val stimuli = SequenceGenerator(Random(42)).generate(config)
        assertEquals(config.trialCount, stimuli.size)
        stimuli.forEach {
            assertTrue(it.position in Stimuli.POSITIONS)
            assertFalse(it.position == Stimuli.CENTER_CELL)
            assertTrue(it.letter in Stimuli.LETTERS)
        }
    }

    @Test
    fun `same seed produces same sequence`() {
        val config = SessionConfig(n = 3)
        assertEquals(
            SequenceGenerator(Random(7)).generate(config),
            SequenceGenerator(Random(7)).generate(config),
        )
    }
}
