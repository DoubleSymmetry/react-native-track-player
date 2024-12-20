package com.doublesymmetry.kotlinaudio.players.components

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultLoadControl.Builder
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MIN_BUFFER_MS
import com.doublesymmetry.kotlinaudio.models.BufferOptions

@OptIn(UnstableApi::class)
fun setupBuffer(bufferConfig: BufferOptions): DefaultLoadControl {
    val multiplier = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / DEFAULT_BUFFER_FOR_PLAYBACK_MS

    val minBuffer = bufferConfig.minBuffer?.takeIf { it != 0 } ?: DEFAULT_MIN_BUFFER_MS
    val maxBuffer = bufferConfig.maxBuffer?.takeIf { it != 0 } ?: DEFAULT_MAX_BUFFER_MS
    val playBuffer = bufferConfig.playBuffer?.takeIf { it != 0 } ?: DEFAULT_BUFFER_FOR_PLAYBACK_MS
    val backBuffer = bufferConfig.backBuffer?.takeIf { it != 0 } ?: DEFAULT_BACK_BUFFER_DURATION_MS

    return Builder()
        .setBufferDurationsMs(minBuffer, maxBuffer, playBuffer, playBuffer * multiplier)
        .setBackBuffer(backBuffer, false)
        .build()
}
