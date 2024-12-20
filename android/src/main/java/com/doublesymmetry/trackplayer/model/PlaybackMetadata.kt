package com.doublesymmetry.trackplayer.model

import androidx.annotation.OptIn
import androidx.media3.common.Metadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import androidx.media3.extractor.metadata.icy.IcyHeaders
import androidx.media3.extractor.metadata.icy.IcyInfo
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.id3.UrlLinkFrame
import androidx.media3.container.MdtaMetadataEntry

@OptIn(UnstableApi::class)
data class PlaybackMetadata(
    val source: String,
    var title: String? = null,
    var url: String? = null,
    var artist: String? = null,
    var album: String? = null,
    var date: String? = null,
    var genre: String? = null
) {
    companion object {
        /**
         * ID3 Metadata (MP3)
         * https://en.wikipedia.org/wiki/ID3
         */
        fun fromId3Metadata(metadata: Metadata): PlaybackMetadata? {
            var handled = false

            var title: String? = null
            var url: String? = null
            var artist: String? = null
            var album: String? = null
            var date: String? = null
            var genre: String? = null

            (0 until metadata.length()).forEach { i ->
                when (val entry = metadata[i]) {
                    is TextInformationFrame -> {
                        when (entry.id.uppercase()) {
                            "TIT2", "TT2" -> {
                                handled = true
                                title = entry.values[0]
                            }
                            "TALB", "TOAL", "TAL" -> {
                                handled = true
                                album = entry.values[0]
                            }
                            "TOPE", "TPE1", "TP1" -> {
                                handled = true
                                artist = entry.values[0]
                            }
                            "TDRC", "TOR" -> {
                                handled = true
                                date = entry.values[0]
                            }
                            "TCON", "TCO" -> {
                                handled = true
                                genre = entry.values[0]
                            }

                        }
                    }
                    is UrlLinkFrame -> {
                        when (entry.id.uppercase()) {
                            "WOAS", "WOAF", "WOAR", "WAR" -> {
                                handled = true;
                                url = entry.url;
                            }
                        }
                    }
                }
            }


            return if (handled) PlaybackMetadata("id3", title, url, artist, album, date, genre) else null
        }

        /**
         * Shoutcast / Icecast metadata (ICY protocol)
         * https://cast.readme.io/docs/icy
         */
        fun fromIcy(metadata: Metadata): PlaybackMetadata? {
            for (i in 0 until metadata.length()) {
                when (val entry = metadata[i]) {
                    is IcyHeaders -> {
                        return PlaybackMetadata("icy-headers", title = entry.name, url = entry.url, genre = entry.genre)
                    }
                    is IcyInfo -> {
                        val artist: String?
                        val title: String?
                        val index =
                            if (entry.title == null) -1 else entry.title!!.indexOf(" - ")
                        if (index != -1) {
                            artist = entry.title!!.substring(0, index)
                            title = entry.title!!.substring(index + 3)
                        } else {
                            artist = null
                            title = entry.title
                        }

                        return PlaybackMetadata("icy", title = title, url = entry.url, artist = artist)
                    }
                }
            }

            return null
        }

        /**
         * Vorbis Comments (Vorbis, FLAC, Opus, Speex, Theora)
         * https://xiph.org/vorbis/doc/v-comment.html
         */
        fun fromVorbisComment(metadata: Metadata): PlaybackMetadata? {
            var handled = false;

            var title: String? = null
            var url: String? = null
            var artist: String? = null
            var album: String? = null
            var date: String? = null
            var genre: String? = null

            for (i in 0 until metadata.length()) {
                val entry = metadata[i]
                if (entry is VorbisComment) {
                    when (entry.key) {
                        "TITLE" -> {
                            handled = true
                            title = entry.value;
                        }
                        "ARTIST" -> {
                            handled = true
                            artist = entry.value;
                        }
                        "ALBUM" -> {
                            handled = true
                            album = entry.value;
                        }
                        "DATE" -> {
                            handled = true
                            date = entry.value
                        }
                        "GENRE" -> {
                            handled = true
                            genre = entry.value
                        }
                        "URL" -> {
                            handled = true
                            url = entry.value
                        }
                    }
                }
            }
            return if (handled) PlaybackMetadata("vorbis-comment", title, url, artist, album, date, genre) else null
        }

        /**
         * QuickTime MDTA metadata (mov, qt)
         * https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/Metadata/Metadata.html
         */
        fun fromQuickTime(metadata: Metadata): PlaybackMetadata? {
            var handled = false;

            var title: String? = null
            var artist: String? = null
            var album: String? = null
            var date: String? = null
            var genre: String? = null

            for (i in 0 until metadata.length()) {
                val entry = metadata[i];
                if (entry is MdtaMetadataEntry) {
                    when (entry.key) {
                        "com.apple.quicktime.title" -> {
                            handled = true
                            title = entry.value.toString();
                        }
                        "com.apple.quicktime.artist" -> {
                            handled = true
                            artist = entry.value.toString();
                        }
                        "com.apple.quicktime.album" -> {
                            handled = true
                            album = entry.value.toString();
                        }
                        "com.apple.quicktime.creationdate" -> {
                            handled = true
                            date = entry.value.toString();
                        }
                        "com.apple.quicktime.genre" -> {
                            handled = true
                            genre = entry.value.toString();
                        }
                    }
                }
            }

            return if (handled) PlaybackMetadata("quicktime", title = title, artist = artist, album = album, date = date, genre = genre) else null
        }
    }
}
