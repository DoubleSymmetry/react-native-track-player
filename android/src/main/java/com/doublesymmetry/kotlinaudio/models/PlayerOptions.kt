@file: OptIn(UnstableApi::class)
package com.doublesymmetry.kotlinaudio.models

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi

data class PlayerOptions(
    val cacheSize: Long = 0,
    val audioContentType: Int = C.AUDIO_CONTENT_TYPE_MUSIC,
    val wakeMode: Int = 0,
    val handleAudioBecomingNoisy: Boolean = true,
    val alwaysShowNext: Boolean = true,
    val handleAudioFocus: Boolean = true,
    var alwaysPauseOnInterruption: Boolean = true,
    var repeatMode: RepeatMode = RepeatMode.ALL,
    val bufferOptions: BufferOptions = BufferOptions(null, null, null, null),
    val parseEmbeddedArtwork: Boolean = false,
    val skipSilence: Boolean = false,
    val nativeExample: Boolean = false,
    val interceptPlayerActionsTriggeredExternally: Boolean = false
)

data class BufferOptions (
    val minBuffer: Int?,
    val maxBuffer: Int?,
    val playBuffer: Int?,
    val backBuffer: Int?,
)

fun setWakeMode(type: Int = 0): Int {
    return when (type) {
        1 -> C.WAKE_MODE_LOCAL
        2 -> C.WAKE_MODE_NETWORK
        else -> C.WAKE_MODE_NONE
    }
}
