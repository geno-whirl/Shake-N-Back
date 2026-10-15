package app.shakenback.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShakeDetectorTest {

    private val rest = 9.81f
    private val jolt = 9.81f * 3

    /** Feeds a sample at [time] and returns whether it fired. */
    private fun ShakeDetector.at(time: Long, z: Float) = onSample(time, 0f, 0f, z)

    @Test
    fun `lying still never fires`() {
        val detector = ShakeDetector()
        assertFalse((0L..5_000L step 20).any { detector.at(it, rest) })
    }

    @Test
    fun `a single jolt is not a shake, even if it spans many samples`() {
        val detector = ShakeDetector()
        assertFalse((0L..200L step 10).any { detector.at(it, jolt) })
        assertFalse(detector.at(220, rest))
    }

    @Test
    fun `two jolts within the window fire once`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, jolt))
        assertFalse(detector.at(100, rest))
        assertEquals(true, detector.at(300, jolt))
    }

    @Test
    fun `jolts too far apart do not fire`() {
        val detector = ShakeDetector()
        detector.at(0, jolt)
        detector.at(100, rest)
        assertFalse(detector.at(1_000, jolt))
    }

    @Test
    fun `cooldown suppresses an immediate second shake`() {
        val detector = ShakeDetector()
        detector.at(0, jolt); detector.at(50, rest)
        assertEquals(true, detector.at(100, jolt)); detector.at(150, rest)
        detector.at(200, jolt); detector.at(250, rest)
        assertFalse(detector.at(300, jolt)); detector.at(350, rest)
        // After the cooldown, a fresh pair fires again.
        detector.at(1_400, jolt); detector.at(1_450, rest)
        assertEquals(true, detector.at(1_500, jolt))
    }
}
