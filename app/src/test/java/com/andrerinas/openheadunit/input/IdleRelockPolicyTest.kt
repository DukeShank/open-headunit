package com.andrerinas.openheadunit.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdleRelockPolicyTest {

    @Test
    fun `the countdown runs for an unlocked projection in front with a time set`() {
        assertTrue(IdleRelockPolicy.shouldRun(showOverLockScreen = true, idleSeconds = 30, held = false, inFront = true))
    }

    @Test
    fun `off, or without show over lock screen, nothing runs`() {
        assertFalse(IdleRelockPolicy.shouldRun(showOverLockScreen = true, idleSeconds = 0, held = false, inFront = true))
        assertFalse(IdleRelockPolicy.shouldRun(showOverLockScreen = false, idleSeconds = 30, held = false, inFront = true))
    }

    @Test
    fun `a unit that is already locked has nothing to lock`() {
        assertFalse(IdleRelockPolicy.shouldRun(showOverLockScreen = true, idleSeconds = 30, held = true, inFront = true))
    }

    @Test
    fun `a projection that is not in front does not lock the unit`() {
        assertFalse(IdleRelockPolicy.shouldRun(showOverLockScreen = true, idleSeconds = 30, held = false, inFront = false))
    }

    @Test
    fun `the choices start with off and rise`() {
        assertEquals(0, IdleRelockPolicy.CHOICES_SECONDS.first())
        assertEquals(IdleRelockPolicy.CHOICES_SECONDS.sorted(), IdleRelockPolicy.CHOICES_SECONDS)
    }

    @Test
    fun `a stored value outside the choices falls to the choice below it`() {
        IdleRelockPolicy.CHOICES_SECONDS.forEach { assertEquals(it, IdleRelockPolicy.normalize(it)) }
        assertEquals(30, IdleRelockPolicy.normalize(45))
        assertEquals(300, IdleRelockPolicy.normalize(100_000))
        assertEquals(0, IdleRelockPolicy.normalize(-5))
        assertEquals(0, IdleRelockPolicy.normalize(7))
    }
}
