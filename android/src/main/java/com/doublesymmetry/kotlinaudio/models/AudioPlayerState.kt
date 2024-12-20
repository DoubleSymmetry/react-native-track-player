package com.doublesymmetry.kotlinaudio.models

enum class AudioPlayerState {
    /** The current [AudioItem] is being loaded for playback. */
    LOADING,

    /** The current [AudioItem] is loaded, and the player is ready to start playing. */
    READY,

    /** The current [AudioItem] is currently buffering. */
    BUFFERING,

    /** The player is paused. */
    PAUSED,

    /** The player is stopped. */
    STOPPED,

    /** The player is playing. */
    PLAYING,

    /** No [AudioItem] is loaded and the player is doing nothing. */
    IDLE,

    /** Playback stopped due to the end of the queue being reached. */
    ENDED,

    /** The player stopped playing due to an error. */
    ERROR
}
