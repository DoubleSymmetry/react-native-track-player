package com.doublesymmetry.kotlinaudio.models

/**
 * Use these events to track when and why the positionMs of an [AudioItem] changes.
 * Examples include changes to [AudioItem] queue, seeking, skipping, etc.
 */
sealed class PositionChangedReason(val oldPosition: Long, val newPosition: Long) {
    /**
     * Position has changed because the player has automatically transitioned to the next [AudioItem].
     *
     * @see [AudioItemTransitionReason]
     */
    class AUTO(oldPosition: Long, newPosition: Long) : PositionChangedReason(oldPosition, newPosition)

    /**
     * Position has changed because of a queue update.
     */
    class QUEUE_CHANGED(oldPosition: Long, newPosition: Long) : PositionChangedReason(oldPosition, newPosition)

    /**
     * Position has changed because a seek has occurred within the current [AudioItem], or another one.
     */
    class SEEK(oldPosition: Long, newPosition: Long) : PositionChangedReason(oldPosition, newPosition)

    /**
     * Position has changed because an attempted seek has failed. This can occur if we tried to see to an invalid positionMs.
     */
    class SEEK_FAILED(oldPosition: Long, newPosition: Long) : PositionChangedReason(oldPosition, newPosition)

    /**
     * Position has changed because a period (example: an ad) has been skipped.
     */
    class SKIPPED_PERIOD(oldPosition: Long, newPosition: Long) : PositionChangedReason(oldPosition, newPosition)

    /**
     * Position has changed for an unknown reason.
     */
    class UNKNOWN(oldPosition: Long, newPosition: Long) : PositionChangedReason(oldPosition, newPosition)
}
