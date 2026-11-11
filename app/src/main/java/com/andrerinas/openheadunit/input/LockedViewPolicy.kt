package com.andrerinas.openheadunit.input

/**
 * What a locked unit shows with "Text view while locked" on: the night overlay (clock, next turn
 * and playing track as text) or the Android Auto picture with the map. The overlay is drawn by
 * this app from data the phone sends beside the picture, so it shows no map, no saved places and
 * none of Android Auto's pop-ups.
 *
 * The choice is only about what this unit draws. Nothing is sent to the phone for it, and the
 * input hold of [LockedInputPolicy] stays as it is in both views.
 */
object LockedViewPolicy {
    enum class Step { SHOW_TEXT, HIDE_TEXT, KEEP }

    /**
     * [engaged] is the input hold of a locked unit, [textView] the setting, [textChosen] the view
     * used last while locked (true for text). [overlayVisible] says whether the night overlay is
     * up now, and [ownsOverlay] whether the locked unit put it up, rather than a gesture of the
     * unlocked projection.
     *
     * Unlocked, or with the setting off, only an overlay the locked unit put up is taken away: one
     * opened by hand keeps its own tap to close.
     */
    fun decide(
        engaged: Boolean,
        textView: Boolean,
        textChosen: Boolean,
        overlayVisible: Boolean,
        ownsOverlay: Boolean
    ): Step = when {
        !engaged || !textView -> if (ownsOverlay && overlayVisible) Step.HIDE_TEXT else Step.KEEP
        textChosen -> if (overlayVisible) Step.KEEP else Step.SHOW_TEXT
        else -> if (overlayVisible) Step.HIDE_TEXT else Step.KEEP
    }
}
