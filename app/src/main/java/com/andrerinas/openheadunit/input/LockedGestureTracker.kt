package com.andrerinas.openheadunit.input

import kotlin.math.abs
import kotlin.math.hypot

/**
 * The two-finger gestures a locked unit still answers. With "Gestures while locked" on: a
 * sideways swipe for the next or previous track, a tap for play or pause. The projection screen
 * turns them into media keys, never into touches, so they can only control playback. With "Text
 * view while locked" on: a swipe down for the text view, a swipe up for the map. Those two change
 * only what this unit draws, and send nothing to the phone.
 *
 * One tracker follows one touch, from the first finger down to the last finger up, and reads the
 * gesture when the first of the two fingers lifts. Plain arithmetic over positions and times, so
 * it can be unit tested.
 */
internal class LockedGestureTracker(density: Float) {
    /** [switchesView] gestures choose what the locked unit shows; the others are media keys. */
    enum class Gesture(val switchesView: Boolean) {
        NEXT_TRACK(false),
        PREVIOUS_TRACK(false),
        PLAY_PAUSE(false),
        TEXT_VIEW(true),
        MAP_VIEW(true)
    }

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
        if (elapsed > SWIPE_MS) return null
        // Right is next, left is previous: the order of the two skip buttons.
        if (isSwipe(dx1, dx2, dy1, dy2)) return if (dx1 > 0f) Gesture.NEXT_TRACK else Gesture.PREVIOUS_TRACK
        // Down draws the text view over the map, up takes it away again.
        if (isSwipe(dy1, dy2, dx1, dx2)) return if (dy1 > 0f) Gesture.TEXT_VIEW else Gesture.MAP_VIEW
        return null
    }

    /** Both fingers the same way along one axis (a pinch moves them apart), far enough, and straight. */
    private fun isSwipe(along1: Float, along2: Float, across1: Float, across2: Float): Boolean {
        val sameWay = along1 * along2 > 0f
        val far = abs(along1) >= travel / 2 && abs(along2) >= travel / 2 && abs(along1 + along2) / 2 >= travel
        val straight = abs(across1) <= sideways && abs(across2) <= sideways
        return sameWay && far && straight
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
