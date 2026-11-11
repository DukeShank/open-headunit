package com.andrerinas.openheadunit.input

/**
 * "Lock again when idle". An unlocked unit that shows the projection never locks by itself,
 * because the projection keeps the screen on. With a time set, the projection screen locks the
 * unit once it has not been touched for that long; shown over the lock screen, the map is back
 * right after, with input held (see [LockedInputPolicy]).
 *
 * Kept free of Android calls so it can be unit tested.
 */
object IdleRelockPolicy {

    /** The times offered in settings, in seconds; 0 is off. */
    val CHOICES_SECONDS: List<Int> = listOf(0, 15, 30, 60, 120, 300)

    /** A stored value outside the choices (an edited backup) counts as the nearest choice below it. */
    fun normalize(seconds: Int): Int = CHOICES_SECONDS.lastOrNull { it <= seconds } ?: 0

    /**
     * Whether the idle countdown should run.
     *
     * @param showOverLockScreen the "Show over lock screen" setting. Without it a locked unit
     *                           shows its lock screen and not the map, so this stays off.
     * @param idleSeconds        the chosen time, 0 for off.
     * @param held               input is already held back: the unit is locked.
     * @param inFront            the projection screen is in front.
     */
    fun shouldRun(showOverLockScreen: Boolean, idleSeconds: Int, held: Boolean, inFront: Boolean): Boolean =
        showOverLockScreen && idleSeconds > 0 && !held && inFront
}
