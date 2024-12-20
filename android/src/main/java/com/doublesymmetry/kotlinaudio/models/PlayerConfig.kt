package com.doublesymmetry.kotlinaudio.models

data class PlayerConfig(
    /**
     * Toggle whether or not a player action triggered from an outside source should be intercepted.
     *
     * The sources can be: media buttons on headphones, Android Wear, Android Auto, Google Assistant, media notification, etc.
     *
     * Setting this to true enables the use of [onPlayerActionTriggeredExternally][com.doublesymmetry.kotlinaudio.event.PlayerEventHolder.onPlayerActionTriggeredExternally] events.
     *
     * **Example**:
     * ```
     *  val player = QueuedAudioPlayer(requireActivity(), playerConfig = PlayerConfig(interceptPlayerActionsTriggeredExternally = true))
     * ```
     */
    var interceptPlayerActionsTriggeredExternally: Boolean = false,

    /**
     * Toggle whether the player should pause automatically when audio is rerouted from a headset to device speakers.
     */
    val handleAudioBecomingNoisy: Boolean = false,

    /**
     * Whether audio focus should be managed automatically. See https://medium.com/google-exoplayer/easy-audio-focus-with-exoplayer-a2dcbbe4640e
     */
    val handleAudioFocus: Boolean = false,
    /**
     * The audio content type.
     */
    val audioContentType: AudioContentType = AudioContentType.MUSIC,

    /**
     * The audio usage.
     */
    val wakeMode: WakeMode = WakeMode.NONE,
)
