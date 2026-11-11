package com.andrerinas.openheadunit.input

import com.andrerinas.openheadunit.input.LockedViewPolicy.Step
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LockedViewPolicyTest {

    private fun decide(
        engaged: Boolean = true,
        textView: Boolean = true,
        textChosen: Boolean = true,
        overlayVisible: Boolean = false,
        ownsOverlay: Boolean = false
    ) = LockedViewPolicy.decide(engaged, textView, textChosen, overlayVisible, ownsOverlay)

    @Test
    fun `a locked unit with the text view chosen puts the overlay up`() {
        assertEquals(Step.SHOW_TEXT, decide())
    }

    @Test
    fun `an overlay that is already up stays`() {
        assertEquals(Step.KEEP, decide(overlayVisible = true, ownsOverlay = true))
        assertEquals(Step.KEEP, decide(overlayVisible = true, ownsOverlay = false))
    }

    @Test
    fun `a locked unit with the map chosen takes the overlay away`() {
        assertEquals(Step.HIDE_TEXT, decide(textChosen = false, overlayVisible = true, ownsOverlay = true))
        assertEquals(Step.HIDE_TEXT, decide(textChosen = false, overlayVisible = true, ownsOverlay = false))
        assertEquals(Step.KEEP, decide(textChosen = false))
    }

    @Test
    fun `unlocking takes away only the overlay the locked unit put up`() {
        assertEquals(Step.HIDE_TEXT, decide(engaged = false, overlayVisible = true, ownsOverlay = true))
        assertEquals(Step.KEEP, decide(engaged = false, overlayVisible = true, ownsOverlay = false))
        assertEquals(Step.KEEP, decide(engaged = false))
    }

    @Test
    fun `with the setting off a locked unit never puts the overlay up`() {
        for (chosen in listOf(true, false)) {
            assertEquals(Step.KEEP, decide(textView = false, textChosen = chosen))
            // One opened by a gesture before the lock is left alone.
            assertEquals(Step.KEEP, decide(textView = false, textChosen = chosen, overlayVisible = true))
        }
    }

    @Test
    fun `no step ever puts the overlay up on an unlocked unit`() {
        for (textView in listOf(true, false)) for (chosen in listOf(true, false))
            for (visible in listOf(true, false)) for (owns in listOf(true, false)) {
                val step = decide(engaged = false, textView = textView, textChosen = chosen,
                    overlayVisible = visible, ownsOverlay = owns)
                assertNotEquals(Step.SHOW_TEXT, step)
            }
    }
}
