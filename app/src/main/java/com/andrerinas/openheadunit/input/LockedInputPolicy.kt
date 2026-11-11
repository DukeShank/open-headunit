package com.andrerinas.openheadunit.input

import android.view.KeyEvent

/**
 * What a person at the unit may still do while the projection is shown over a locked unit
 * ("Show over lock screen" on, and the device needs its PIN, pattern or password).
 *
 * The answer is: nothing that can open anything on the phone. Android Auto treats the head unit
 * as trusted, so any touch or scroll that gets through could reach calls, messages or contacts,
 * and this app cannot see what a touch would land on. A key is different: it has one fixed
 * meaning. Two kinds are let through:
 *  - Android Auto's navigation key, which this app sends itself when it locks, so the map is on
 *    screen instead of whatever was open before;
 *  - with "Gestures while locked" on, the media keys in [MEDIA_KEYCODES], which can only control
 *    playback, the same as the media controls of a phone's own lock screen.
 *
 * Kept free of Android calls so it can be unit tested (the key codes are compile-time constants).
 * [engaged] and [mediaKeys] are written by the projection activity on the main thread and read by
 * the transport when it sends, hence volatile.
 */
object LockedInputPolicy {

    /** Android Auto's "show the map" key; the Keymap screen lists it as Navigation. */
    const val KEYCODE_AA_NAVIGATION = 65538

    /** The media keys a locked unit may send when "Gestures while locked" is on. */
    val MEDIA_KEYCODES: Set<Int> = setOf(
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_MEDIA_NEXT,
        KeyEvent.KEYCODE_MEDIA_PREVIOUS
    )

    /** True while input to the phone is held back. */
    @Volatile
    var engaged: Boolean = false

    /** The "Gestures while locked" setting, as the projection screen last read it. */
    @Volatile
    var mediaKeys: Boolean = false

    /**
     * Whether the projection should hold input back.
     *
     * @param enabled      the "Show over lock screen" setting.
     * @param deviceLocked the unit needs authentication (PIN, pattern, password). Null when the
     *                     system would not say; that counts as locked.
     * @param screenOff    the panel just went dark. Counts as locked until the next check, so a
     *                     unit that locks a few seconds after the screen goes off is never open in
     *                     between.
     */
    fun shouldEngage(enabled: Boolean, deviceLocked: Boolean?, screenOff: Boolean): Boolean =
        enabled && (screenOff || deviceLocked != false)

    /**
     * Whether [shouldEngage] needs the lock state at all. Asking for it is a call into the system,
     * and the projection asks on every new touch; with the setting off, or with the panel just
     * gone dark, the answer changes nothing.
     */
    fun needsLockState(enabled: Boolean, screenOff: Boolean): Boolean = enabled && !screenOff

    /**
     * Whether one input event may go to the phone.
     *
     * @param mediaKeys the "Gestures while locked" setting.
     * @param keycode   the Android Auto keycode for a key event, null for touch, scroll and any
     *                  other input event.
     */
    fun allows(engaged: Boolean, mediaKeys: Boolean, keycode: Int?): Boolean =
        !engaged || keycode == KEYCODE_AA_NAVIGATION ||
            (mediaKeys && keycode != null && keycode in MEDIA_KEYCODES)
}
