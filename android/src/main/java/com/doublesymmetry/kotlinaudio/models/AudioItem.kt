package com.doublesymmetry.kotlinaudio.models

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

open class AudioItem(
    open var audioUrl: String,
    open val type: MediaType = MediaType.DEFAULT,
    open var artist: String? = null,
    open var title: String? = null,
    open var albumTitle: String? = null,
    open val artwork: String? = null,
    open val duration: Long? = null,
    open val options: AudioItemOptions? = null,
    open val mediaId: String? = null
) {
    fun toMediaItem(): MediaItem {
        val extras = Bundle().apply {
            options?.headers?.let {
                putSerializable("headers", HashMap(it))
            }
            options?.userAgent?.let {
                putString("user-agent", it)
            }
            options?.resourceId?.let {
                putInt("resource-id", it)
            }
            putString("type", type.toString())
            putString("uri", audioUrl)
        }
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(Uri.parse(artwork))
            .setExtras(extras)
            .build()
        return MediaItem.Builder()
            .setMediaId(mediaId ?: audioUrl)
            .setUri(audioUrl)
            .setMediaMetadata(mediaMetadata)
            .setTag(this)
            .build()
    }

    companion object {
        fun fromMediaItem(item: MediaItem): AudioItem {
            return item.localConfiguration!!.tag as AudioItem
        }
    }
}

data class AudioItemOptions(
    val headers: MutableMap<String, String>? = null,
    val userAgent: String? = null,
    val resourceId: Int? = null
)

enum class MediaType(val value: String) {
    /**
     * The default media type. Should be used for streams over HTTP or files
     */
    DEFAULT("default"),

    /**
     * The DASH media type for adaptive streams. Should be used with DASH manifests.
     */
    DASH("dash"),

    /**
     * The HLS media type for adaptive streams. Should be used with HLS playlists.
     */
    HLS("hls"),

    /**
     * The SmoothStreaming media type for adaptive streams. Should be used with SmoothStreaming manifests.
     */
    SMOOTH_STREAMING("smoothstreaming");
}