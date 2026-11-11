package com.andrerinas.openheadunit.input

import kotlin.math.abs
import kotlin.math.hypot

/**
 * The two-finger gestures a locked unit still answers when "Gestures while locked" is on: a
 * sideways swipe for the next or previous track, a tap for play or pause. The projection screen
 * turns them into media keys, never into touches, so they can only control playback.
 *
 * One tracker follows one touch, from the first finger down to the last finger up, and reads the
 * gesture when the first of the two fingers lifts. Plain arithmetic over positions and times, so
 * it can be unit tested.
 */
internal class LockedGestureTracker(density: Float) {
    enum class Gesture { NEXT_TRACK, PREVIOUS_TRACK, PLAY_PAUSE }

    private val travel = GestureDistances.travelPx(density)
    private val sideways = GestureDistances.sidewaysLimitPx(density)
    private val still = maxOf(STILL_FLOOR_PX, STILL_DP * density)

    private var startX1 = 0f
    private var startY1 = 0f
    private var startX2 = 0f
    private var startY2 = 0f
    private var lastX1 = 0f
    private var lastY1 = 0f
    private var lastX2 = 0f
    private var lastY2 = 0f
    private var secondDownMs = 0L

    /** Two fingers are down and nothing has been decided yet. */
    private var tracking = false

    /** True once a second finger has touched: the touch was a gesture attempt, not a stray tap. */
    var sawSecondFinger = false
        private set

    /** The second finger lands at [nowMs]; positions are the two fingers in pointer order. */
    fun onSecondDown(x1: Float, y1: Float, x2: Float, y2: Float, nowMs: Long) {
        // A finger that lifted and came back is not one gesture.
        if (sawSecondFinger) {
            tracking = false
            return
        }
        sawSecondFinger = true
        tracking = true
        startX1 = x1
        startY1 = y1
        startX2 = x2
        startY2 = y2
        lastX1 = x1
        lastY1 = y1
        lastX2 = x2
        lastY2 = y2
        secondDownMs = nowMs
    }

    /** A third finger, or anything else that makes this touch no gesture. */
    fun spoil() {
        sawSecondFinger = true
        tracking = false
    }

    fun onMove(x1: Float, y1: Float, x2: Float, y2: Float) {
        if (!tracking) return
        lastX1 = x1
        lastY1 = y1
        lastX2 = x2
        lastY2 = y2
    }

    /** The gesture the two fingers made, read when the first of them lifts at [nowMs]; null for none. */
    fun onLift(nowMs: Long): Gesture? {
        if (!tracking) return null
        tracking = false
        val dx1 = lastX1 - startX1
        val dy1 = lastY1 - startY1
        val dx2 = lastX2 - startX2
        val dy2 = lastY2 - startY2
        val elapsed = nowMs - secondDownMs
        if (elapsed <= TAP_MS && hypot(dx1, dy1) <= still && hypot(dx2, dy2) <= still) {
            return Gesture.PLAY_PAUSE
        }
        // Both fingers the same way (a pinch moves them apart), far enough, and level.
        val sameWay = dx1 * dx2 > 0f
        val far = abs(dx1) >= travel / 2 && abs(dx2) >= travel / 2 && abs(dx1 + dx2) / 2 >= travel
        val level = abs(dy1) <= sideways && abs(dy2) <= sideways
        if (elapsed > SWIPE_MS || !sameWay || !far || !level) return null
        // Right is next, left is previous: the order of the two skip buttons.
        return if (dx1 > 0f) Gesture.NEXT_TRACK else Gesture.PREVIOUS_TRACK
    }

    companion object {
        /** Longest two-finger tap, from the second finger's down to the first lift. */
        const val TAP_MS = 400L

        /** Longest swipe, over the same span. */
        const val SWIPE_MS = 1_200L

        /** How far a finger may drift during a tap; wide, for gloves. */
        private const val STILL_DP = 24f
        private const val STILL_FLOOR_PX = 48f
    }
}
