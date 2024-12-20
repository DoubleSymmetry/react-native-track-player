package com.doublesymmetry.trackplayer.utils

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

fun buildMediaItem(
    title: String? = null,
    subtitle: String? = null,
    mediaId: String,
    isPlayable: Boolean,
    subtitleConfigurations: List<MediaItem.SubtitleConfiguration> = mutableListOf(),
    album: String? = null,
    artist: String? = null,
    genre: String? = null,
    sourceUri: Uri? = null,
    imageUri: Uri? = null,
    extras: Bundle? = null
): MediaItem {
    val metadata =
        MediaMetadata.Builder()
            .setAlbumTitle(album)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setArtist(artist)
            .setGenre(genre)
            .setIsBrowsable(!isPlayable)
            .setIsPlayable(isPlayable)
            .setArtworkUri(imageUri)
            .setMediaType(if (isPlayable) MediaMetadata.MEDIA_TYPE_MUSIC else MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
            .setExtras(extras)
            .build()

    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setSubtitleConfigurations(subtitleConfigurations)
        .setMediaMetadata(metadata)
        .setUri(sourceUri)
        .build()
}