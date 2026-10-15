package app.shakenback.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionTest {

    // 1-back over 5 trials. Position matches at trials 1 and 3; audio match at trial 2.
    private val config = SessionConfig(n = 1, trialCount = 5, matchesPerModality = 1)
    private val stimuli = listOf(
        Stimulus(0, 'C'),
        Stimulus(0, 'H'),
        Stimulus(1, 'H'),
        Stimulus(1, 'K'),
        Stimulus(2, 'L'),
    )

    @Test
    fun `warm-up trials and duplicate presses are ignored`() {
        val session = Session(config, stimuli)
        assertNull(session.respond(0, Modality.POSITION))
        assertEquals(true, session.respond(1, Modality.POSITION))
        assertNull(session.respond(1, Modality.POSITION))
    }

    @Test
    fun `scores hits misses false alarms and correct rejections`() {
        val session = Session(config, stimuli)
        session.respond(1, Modality.POSITION) // hit
        session.respond(4, Modality.POSITION) // false alarm; trial 3 missed
        session.respond(2, Modality.AUDIO) // hit

        val result = session.result()
        assertEquals(ModalityScore(hits = 1, misses = 1, falseAlarms = 1, correctRejections = 1), result.position)
        assertEquals(ModalityScore(hits = 1, misses = 0, falseAlarms = 0, correctRejections = 3), result.audio)
        assertEquals(1.0 / 3, result.position.accuracy, 1e-9)
        assertEquals(2.0 / 4, result.overall, 1e-9)
    }

    @Test
    fun `no responses and no targets scores perfect`() {
        val quiet = List(5) { Stimulus(it % 2 * 2, if (it % 2 == 0) 'C' else 'H') }
        val result = Session(config, quiet).result()
        assertEquals(1.0, result.overall, 0.0)
    }
}
