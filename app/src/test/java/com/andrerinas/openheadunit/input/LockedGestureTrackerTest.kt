package com.andrerinas.openheadunit.input

import com.andrerinas.openheadunit.input.LockedGestureTracker.Gesture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** At density 1 the sizes are their pixel floors: 200 px travel, 100 px sideways, 48 px still. */
class LockedGestureTrackerTest {

    /** Two fingers land at (500, 300) and (600, 300) at time 0. */
    private fun twoDown() = LockedGestureTracker(1f).apply { onSecondDown(500f, 300f, 600f, 300f, 0L) }

    /** Moves both fingers by the same offset. */
    private fun LockedGestureTracker.moveBoth(dx: Float, dy: Float = 0f) =
        onMove(500f + dx, 300f + dy, 600f + dx, 300f + dy)

    @Test
    fun `a quick still tap with two fingers is play or pause`() {
        assertEquals(Gesture.PLAY_PAUSE, twoDown().onLift(200L))
    }

    @Test
    fun `a gloved tap may drift a little`() {
        val tracker = twoDown()
        tracker.onMove(520f, 310f, 585f, 290f)
        assertEquals(Gesture.PLAY_PAUSE, tracker.onLift(350L))
    }

    @Test
    fun `two fingers that rest too long are not a tap`() {
        assertNull(twoDown().onLift(LockedGestureTracker.TAP_MS + 1))
    }

    @Test
    fun `a swipe to the right is the next track`() {
        val tracker = twoDown()
        tracker.moveBoth(260f)
        assertEquals(Gesture.NEXT_TRACK, tracker.onLift(300L))
    }

    @Test
    fun `a swipe to the left is the previous track`() {
        val tracker = twoDown()
        tracker.moveBoth(-260f, dy = 40f)
        assertEquals(Gesture.PREVIOUS_TRACK, tracker.onLift(600L))
    }

    @Test
    fun `a short swipe is nothing`() {
        val tracker = twoDown()
        tracker.moveBoth(-120f)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `a slow swipe is nothing`() {
        val tracker = twoDown()
        tracker.moveBoth(-260f)
        assertNull(tracker.onLift(LockedGestureTracker.SWIPE_MS + 1))
    }

    @Test
    fun `a swipe that wanders up or down is nothing`() {
        val tracker = twoDown()
        tracker.moveBoth(-260f, dy = 150f)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `a swipe down is the text view`() {
        val tracker = twoDown()
        tracker.moveBoth(0f, dy = 260f)
        assertEquals(Gesture.TEXT_VIEW, tracker.onLift(300L))
    }

    @Test
    fun `a swipe up is the map view`() {
        val tracker = twoDown()
        tracker.moveBoth(40f, dy = -260f)
        assertEquals(Gesture.MAP_VIEW, tracker.onLift(600L))
    }

    @Test
    fun `a short or slow swipe down is nothing`() {
        val short = twoDown()
        short.moveBoth(0f, dy = 120f)
        assertNull(short.onLift(300L))
        val slow = twoDown()
        slow.moveBoth(0f, dy = 260f)
        assertNull(slow.onLift(LockedGestureTracker.SWIPE_MS + 1))
    }

    @Test
    fun `a diagonal swipe is nothing`() {
        val tracker = twoDown()
        tracker.moveBoth(260f, dy = 260f)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `two fingers moving apart up and down are nothing`() {
        val tracker = twoDown()
        tracker.onMove(500f, 40f, 600f, 560f)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `only the view gestures switch the view`() {
        assertEquals(setOf(Gesture.TEXT_VIEW, Gesture.MAP_VIEW), Gesture.values().filter { it.switchesView }.toSet())
    }

    @Test
    fun `a pinch is nothing`() {
        val tracker = twoDown()
        tracker.onMove(250f, 300f, 850f, 300f)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `one finger dragging while the other rests is nothing`() {
        val tracker = twoDown()
        tracker.onMove(500f, 300f, 1100f, 300f)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `a third finger spoils the touch`() {
        val tracker = twoDown()
        tracker.spoil()
        tracker.moveBoth(-260f)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `a gesture is read once`() {
        val tracker = twoDown()
        assertEquals(Gesture.PLAY_PAUSE, tracker.onLift(100L))
        assertNull(tracker.onLift(150L))
    }

    @Test
    fun `a finger that lifts and comes back does not start a second gesture`() {
        val tracker = twoDown()
        assertEquals(Gesture.PLAY_PAUSE, tracker.onLift(100L))
        tracker.onSecondDown(500f, 300f, 600f, 300f, 200L)
        assertNull(tracker.onLift(300L))
    }

    @Test
    fun `one finger alone never saw a second finger`() {
        val tracker = LockedGestureTracker(1f)
        assertFalse(tracker.sawSecondFinger)
        assertNull(tracker.onLift(100L))
        assertTrue(twoDown().sawSecondFinger)
    }
}
