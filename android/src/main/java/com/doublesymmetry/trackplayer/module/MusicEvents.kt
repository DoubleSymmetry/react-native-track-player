package com.doublesymmetry.trackplayer.module

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter

/**
 * @author Milen Pivchev @mpivchev
 */
class MusicEvents(private val reactContext: ReactContext) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = intent.getStringExtra("event")
        val data = intent.getBundleExtra("data")
        val map = if (data != null) Arguments.fromBundle(data) else null
        reactContext.emitDeviceEvent(event!!, map)
    }

    companion object {
        // Media Control Events
        const val BUTTON_PLAY = "remote-play"
        const val BUTTON_PLAY_PAUSE = "remote-play-pause"
        const val BUTTON_PLAY_FROM_ID = "remote-play-id"
        const val BUTTON_PLAY_FROM_SEARCH = "remote-play-search"
        const val BUTTON_PAUSE = "remote-pause"
        const val BUTTON_STOP = "remote-stop"
        const val BUTTON_SKIP = "remote-skip"
        const val BUTTON_SKIP_NEXT = "remote-next"
        const val BUTTON_SKIP_PREVIOUS = "remote-previous"
        const val BUTTON_SEEK_TO = "remote-seek"
        const val BUTTON_SET_RATING = "remote-set-rating"
        const val BUTTON_JUMP_FORWARD = "remote-jump-forward"
        const val BUTTON_JUMP_BACKWARD = "remote-jump-backward"
        const val BUTTON_DUCK = "remote-duck"
        const val BUTTON_BROWSE = "remote-browse"

        // Playback Events
        const val PLAYBACK_PLAY_WHEN_READY_CHANGED = "playback-play-when-ready-changed"
        const val PLAYBACK_STATE = "playback-state"
        const val PLAYBACK_ACTIVE_TRACK_CHANGED = "playback-active-track-changed"
        const val PLAYBACK_QUEUE_ENDED = "playback-queue-ended"
        const val PLAYBACK_METADATA = "playback-metadata-received"
        const val PLAYBACK_PROGRESS_UPDATED = "playback-progress-updated"
        const val PLAYBACK_ERROR = "playback-error"
        const val PLAYBACK_RESUME = "android-playback-resume"

        // Metadata Events
        const val METADATA_CHAPTER_RECEIVED = "metadata-chapter-received"
        const val METADATA_TIMED_RECEIVED = "metadata-timed-received"
        const val METADATA_COMMON_RECEIVED = "metadata-common-received"
        const val METADATA_PAYLOAD_KEY = "metadata"

        // Other
        const val PLAYER_ERROR = "player-error"
        const val CONNECTOR_CONNECTED = "android-controller-connected"
        const val CONNECTOR_DISCONNECTED = "android-controller-disconnected"

        const val EVENT_INTENT = "com.doublesymmetry.trackplayer.event"
    }
}
