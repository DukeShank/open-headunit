package com.andrerinas.openheadunit.input

import android.view.MotionEvent

/**
 * Reads the two fingers of a locked unit's touch into a [LockedGestureTracker].
 *
 * Only how far each finger moves matters to the tracker, so the positions need no common frame
 * with the projection overlay; the projection screen itself reads no coordinates for this.
 */
internal object LockedGestureFeed {

    /** Feeds one event between the first finger's down and the last finger's up; returns a finished gesture. */
    fun feed(tracker: LockedGestureTracker, event: MotionEvent): LockedGestureTracker.Gesture? {
        val twoDown = event.pointerCount == 2
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN ->
                if (twoDown) {
                    tracker.onSecondDown(event.getX(0), event.getY(0), event.getX(1), event.getY(1), event.eventTime)
                } else {
                    tracker.spoil()
                }
            MotionEvent.ACTION_MOVE ->
                if (twoDown) tracker.onMove(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
            MotionEvent.ACTION_POINTER_UP ->
                if (twoDown) {
                    tracker.onMove(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
                    return tracker.onLift(event.eventTime)
                }
        }
        return null
    }
}
