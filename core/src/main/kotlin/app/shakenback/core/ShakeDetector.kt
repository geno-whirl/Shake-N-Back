package app.shakenback.core

import kotlin.math.sqrt

/**
 * Detects a deliberate shake from raw accelerometer samples (m/s², gravity
 * included). A shake is [peaksRequired] separate jolts above [thresholdG]
 * within [windowMillis], so a single bump or a tap on the screen won't fire.
 */
class ShakeDetector(
    private val thresholdG: Float = 2.5f,
    private val peaksRequired: Int = 2,
    private val windowMillis: Long = 800,
    private val cooldownMillis: Long = 1_200,
) {
    private val peaks = ArrayDeque<Long>()
    private var aboveThreshold = false
    private var lastShakeMillis: Long? = null

    /** Feeds one sample; returns true when it completes a shake. */
    fun onSample(timeMillis: Long, x: Float, y: Float, z: Float): Boolean {
        val gForce = sqrt(x * x + y * y + z * z) / STANDARD_GRAVITY
        val wasAbove = aboveThreshold
        aboveThreshold = gForce >= thresholdG
        // Count each jolt once, on its rising edge, however many samples it spans.
        if (!aboveThreshold || wasAbove) return false

        val last = lastShakeMillis
        if (last != null && timeMillis - last < cooldownMillis) return false

        peaks.addLast(timeMillis)
        while (timeMillis - peaks.first() > windowMillis) peaks.removeFirst()
        if (peaks.size < peaksRequired) return false

        peaks.clear()
        lastShakeMillis = timeMillis
        return true
    }

    private companion object {
        const val STANDARD_GRAVITY = 9.80665f
    }
}
