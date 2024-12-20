package com.doublesymmetry.kotlinaudio.models

/**
 * Use these events to track when and why an [AudioItem] transitions to another.
 * Examples of an audio transition include changes to [AudioItem] queue, an [AudioItem] on repeat, skipping an [AudioItem], or simply when the [AudioItem] has finished.
 */
sealed class AudioItemTransitionReason(val oldPosition: Long) {
    /**
     * Playback has automatically transitioned to the next [AudioItem].
     *
     * This reason also indicates a transition caused by another player.
     */
    class AUTO(oldPosition: Long) : AudioItemTransitionReason(oldPosition)

    /**
     * A seek to another [AudioItem] has occurred. Usually triggered when calling
     * [QueuedAudioPlayer.next][com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer.next]
     * or [QueuedAudioPlayer.previous][com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer.previous].
     */
    class SEEK_TO_ANOTHER_AUDIO_ITEM(oldPosition: Long) : AudioItemTransitionReason(oldPosition)

    /**
     * The [AudioItem] has been repeated.
     */
    class REPEAT(oldPosition: Long) : AudioItemTransitionReason(oldPosition)

    /**
     * The current [AudioItem] has changed because of a change in the queue. This can either be if
     * the [AudioItem] previously being played has been removed, or when the queue becomes non-empty
     * after being empty.
     */
    class QUEUE_CHANGED(oldPosition: Long) : AudioItemTransitionReason(oldPosition)
}
