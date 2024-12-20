package com.doublesymmetry.trackplayer.module

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.net.Uri
import android.support.v4.media.RatingCompat
import androidx.media3.common.MediaItem
import androidx.media.utils.MediaConstants
import androidx.media3.common.MediaMetadata
import com.doublesymmetry.kotlinaudio.models.Capability
import com.doublesymmetry.kotlinaudio.models.RepeatMode
import com.doublesymmetry.trackplayer.model.State
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.EVENT_INTENT
import com.doublesymmetry.trackplayer.service.MusicService
import com.doublesymmetry.trackplayer.utils.AppForegroundTracker
import com.doublesymmetry.trackplayer.utils.RejectionException
import com.facebook.react.bridge.*
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.doublesymmetry.trackplayer.utils.buildMediaItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.annotation.Nonnull


/**
 * @author Milen Pivchev @mpivchev
 */
class MusicModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),
    ServiceConnection {
    private lateinit var browser: MediaBrowser
    private var playerOptions: Bundle? = null
    private var isServiceBound = false
    private var playerSetUpPromise: Promise? = null
    private val scope = MainScope()
    private lateinit var musicService: MusicService
    private val context = reactContext

    @Nonnull
    override fun getName(): String {
        return "TrackPlayerModule"
    }

    override fun initialize() {
        AppForegroundTracker.start()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        launchInScope {
            // If a binder already exists, don't get a new one
            if (!::musicService.isInitialized) {
                val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
                musicService = binder.service
                musicService.setupPlayer(playerOptions)
                playerSetUpPromise?.resolve(null)
            }

            isServiceBound = true
        }
    }

    /**
     * Called when a connection to the Service has been lost.
     */
    override fun onServiceDisconnected(name: ComponentName) {
        launchInScope {
            isServiceBound = false
        }
    }

    /**
     * Checks wither service is bound, or rejects. Returns whether promise was rejected.
     */
    private fun verifyServiceBoundOrReject(promise: Promise): Boolean {
        if (!isServiceBound) {
            promise.reject(
                "player_not_initialized",
                "The player is not initialized. Call setupPlayer first."
            )
            return true
        }

        return false
    }

    private fun bundleToTrack(bundle: Bundle): Track {
        return Track(context, bundle, musicService.ratingType)
    }

    private fun hashmapToMediaItem(hashmap: HashMap<String, String>): MediaItem {
        val mediaUri = hashmap["mediaUri"]
        val iconUri = hashmap["iconUri"]

        val extras = Bundle()
        hashmap["groupTitle"]?.let {
            extras.putString(
                MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_GROUP_TITLE, it)
        }
        hashmap["contentStyle"]?.toInt()?.let {
            extras.putInt(
                MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_SINGLE_ITEM, it)
        }
        hashmap["childrenPlayableContentStyle"]?.toInt()?.let {
            extras.putInt(
                MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE, it)
        }
        hashmap["childrenBrowsableContentStyle"]?.toInt()?.let {
            extras.putInt(
                MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, it)
        }

        // playbackProgress should contain a string representation of a number between 0 and 1 if present
        hashmap["playbackProgress"]?.toDouble()?.let {
            if (it > 0.98) {
                extras.putInt(
                    MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_STATUS,
                    MediaConstants.DESCRIPTION_EXTRAS_VALUE_COMPLETION_STATUS_FULLY_PLAYED)
            } else if (it == 0.0) {
                extras.putInt(
                    MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_STATUS,
                    MediaConstants.DESCRIPTION_EXTRAS_VALUE_COMPLETION_STATUS_NOT_PLAYED)
            } else {
                extras.putInt(
                    MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_STATUS,
                    MediaConstants.DESCRIPTION_EXTRAS_VALUE_COMPLETION_STATUS_PARTIALLY_PLAYED)
                extras.putDouble(
                    MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_PERCENTAGE, it)
            }
        }
        return buildMediaItem(
            isPlayable = hashmap["playable"]?.toInt() != 1,
            title = hashmap["title"],
            mediaId = hashmap["mediaId"] ?: "no-media-id",
            imageUri = if (iconUri != null) Uri.parse(iconUri) else null,
            artist = hashmap["subtitle"],
            subtitle = hashmap["subtitle"],
            sourceUri = if (mediaUri != null) Uri.parse(mediaUri) else null,
            extras = extras
        )
    }

    private fun readableArrayToMediaItems(data: ArrayList<HashMap<String, String>>): MutableList<MediaItem> {
        return data.map {
            hashmapToMediaItem(it)
        }.toMutableList()
    }

    private fun rejectWithException(callback: Promise, exception: Exception) {
        when (exception) {
            is RejectionException -> {
                callback.reject(exception.code, exception)
            }
            else -> {
                callback.reject("runtime_exception", exception)
            }
        }
    }

    private fun readableArrayToTrackList(data: ReadableArray?): MutableList<Track> {
        val bundleList = Arguments.toList(data)
        if (bundleList !is ArrayList) {
            throw RejectionException("invalid_parameter", "Was not given an array of tracks")
        }
        return bundleList.map {
            if (it is Bundle) {
                bundleToTrack(it)
            } else {
                throw RejectionException(
                    "invalid_track_object",
                    "Track was not a dictionary type"
                )
            }
        }.toMutableList()
    }

    /* ****************************** API ****************************** */
    override fun getConstants(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            // Capabilities
            this["CAPABILITY_PLAY"] = Capability.PLAY.ordinal
            this["CAPABILITY_PLAY_FROM_ID"] = Capability.PLAY_FROM_ID.ordinal
            this["CAPABILITY_PLAY_FROM_SEARCH"] = Capability.PLAY_FROM_SEARCH.ordinal
            this["CAPABILITY_PAUSE"] = Capability.PAUSE.ordinal
            this["CAPABILITY_STOP"] = Capability.STOP.ordinal
            this["CAPABILITY_SEEK_TO"] = Capability.SEEK_TO.ordinal
            this["CAPABILITY_SKIP"] = OnErrorAction.SKIP.ordinal
            this["CAPABILITY_SKIP_TO_NEXT"] = Capability.SKIP_TO_NEXT.ordinal
            this["CAPABILITY_SKIP_TO_PREVIOUS"] = Capability.SKIP_TO_PREVIOUS.ordinal
            this["CAPABILITY_SET_RATING"] = Capability.SET_RATING.ordinal
            this["CAPABILITY_JUMP_FORWARD"] = Capability.JUMP_FORWARD.ordinal
            this["CAPABILITY_JUMP_BACKWARD"] = Capability.JUMP_BACKWARD.ordinal

            // States
            this["STATE_NONE"] = State.None.state
            this["STATE_READY"] = State.Ready.state
            this["STATE_PLAYING"] = State.Playing.state
            this["STATE_PAUSED"] = State.Paused.state
            this["STATE_STOPPED"] = State.Stopped.state
            this["STATE_BUFFERING"] = State.Buffering.state
            this["STATE_LOADING"] = State.Loading.state

            // Rating Types
            this["RATING_HEART"] = RatingCompat.RATING_HEART
            this["RATING_THUMBS_UP_DOWN"] = RatingCompat.RATING_THUMB_UP_DOWN
            this["RATING_3_STARS"] = RatingCompat.RATING_3_STARS
            this["RATING_4_STARS"] = RatingCompat.RATING_4_STARS
            this["RATING_5_STARS"] = RatingCompat.RATING_5_STARS
            this["RATING_PERCENTAGE"] = RatingCompat.RATING_PERCENTAGE

            // Repeat Modes
            this["REPEAT_OFF"] = Player.REPEAT_MODE_OFF
            this["REPEAT_TRACK"] = Player.REPEAT_MODE_ONE
            this["REPEAT_QUEUE"] = Player.REPEAT_MODE_ALL
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @ReactMethod
    fun setupPlayer(data: ReadableMap?, promise: Promise) {
        if (isServiceBound) {
            promise.reject(
                "player_already_initialized",
                "The player has already been initialized via setupPlayer."
            )
            return
        }

        val bundledData = Arguments.toBundle(data)

        playerSetUpPromise = promise
        playerOptions = bundledData

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                MusicEvents(context),
                IntentFilter(EVENT_INTENT), Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(
                MusicEvents(context),
                IntentFilter(EVENT_INTENT)
            )
        }

        val musicModule = this
        try {
            Intent(context, MusicService::class.java).also { intent ->
                context.bindService(intent, musicModule, Context.BIND_AUTO_CREATE)
                val sessionToken =
                    SessionToken(context, ComponentName(context, MusicService::class.java))
                val browserFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()
                // browser = browserFuture.get()
            }
        } catch (exception: Exception) {
            Timber.w(exception, "Could not initialize service")
            throw exception
        }
    }

    @ReactMethod
    fun updateOptions(data: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        val options = Arguments.toBundle(data)

        options?.let {
            musicService.updateOptions(it)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun add(data: ReadableArray?, insertBeforeIndex: Int, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        try {
            val tracks = readableArrayToTrackList(data);
            if (insertBeforeIndex < -1 || insertBeforeIndex > musicService.tracks.size) {
                callback.reject("index_out_of_bounds", "The track index is out of bounds")
                return@launchInScope
            }
            val index = if (insertBeforeIndex == -1) musicService.tracks.size else insertBeforeIndex
            musicService.add(
                tracks,
                index
            )
            callback.resolve(index)
        } catch (exception: Exception) {
            rejectWithException(callback, exception)
        }
    }

    @ReactMethod
    fun load(data: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        if (data == null) {
            callback.resolve(null)
            return@launchInScope
        }
        val bundle = Arguments.toBundle(data);
        if (bundle is Bundle) {
            musicService.load(bundleToTrack(bundle))
            callback.resolve(null)
        } else {
            callback.reject("invalid_track_object", "Track was not a dictionary type")
        }
    }

    @ReactMethod
    fun move(fromIndex: Int, toIndex: Int, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        musicService.move(fromIndex, toIndex)
        callback.resolve(null)
    }

    @ReactMethod
    fun remove(data: ReadableArray?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        val inputIndexes = Arguments.toList(data)
        if (inputIndexes != null) {
            val size = musicService.tracks.size
            val indexes: ArrayList<Int> = ArrayList();
            for (inputIndex in inputIndexes) {
                val index = if (inputIndex is Int) inputIndex else inputIndex.toString().toInt()
                if (index < 0 || index >= size) {
                    callback.reject(
                        "index_out_of_bounds",
                        "One or more indexes was out of bounds"
                    )
                    return@launchInScope
                }
                indexes.add(index)
            }
            musicService.remove(indexes)
        }
        callback.resolve(null)
    }

    @ReactMethod
    fun updateMetadataForTrack(index: Int, map: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        if (index < 0 || index >= musicService.tracks.size) {
            callback.reject("index_out_of_bounds", "The index is out of bounds")
            return@launchInScope
        }

        Arguments.toBundle(map)?.let {
            musicService.updateMetadataForTrack(index, it)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun updateNowPlayingMetadata(map: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        if (musicService.tracks.isEmpty()) {
            callback.reject("no_current_item", "There is no current item in the player")
            return@launchInScope
        }

        Arguments.toBundle(map)?.let {
            musicService.updateNowPlayingMetadata(it)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun removeUpcomingTracks(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.removeUpcomingTracks()
        callback.resolve(null)
    }

    @ReactMethod
    fun skip(index: Int, initialTime: Float, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.skip(index)

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun skipToNext(initialTime: Float, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.skipToNext()

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun skipToPrevious(initialTime: Float, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.skipToPrevious()

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun reset(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.stop()
        delay(300) // Allow playback to stop
        musicService.clear()

        callback.resolve(null)
    }

    @ReactMethod
    fun play(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.play()
        callback.resolve(null)
    }

    @ReactMethod
    fun pause(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.pause()
        callback.resolve(null)
    }

    @ReactMethod
    fun stop(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.stop()
        callback.resolve(null)
    }

    @ReactMethod
    fun seekTo(seconds: Float, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.seekTo(seconds)
        callback.resolve(null)
    }

    @ReactMethod
    fun seekBy(offset: Float, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.seekBy(offset)
        callback.resolve(null)
    }

    @ReactMethod
    fun retry(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.retry()
        callback.resolve(null)
    }

    @ReactMethod
    fun setVolume(volume: Float, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.setVolume(volume)
        callback.resolve(null)
    }

    @ReactMethod
    fun getVolume(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getVolume())
    }

    @ReactMethod
    fun setRate(rate: Float, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.setRate(rate)
        callback.resolve(null)
    }

    @ReactMethod
    fun getRate(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getRate())
    }

    @ReactMethod
    fun setRepeatMode(mode: Int, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.setRepeatMode(RepeatMode.fromOrdinal(mode))
        callback.resolve(null)
    }

    @ReactMethod
    fun getRepeatMode(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getRepeatMode().ordinal)
    }

    @ReactMethod
    fun setPlayWhenReady(playWhenReady: Boolean, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.playWhenReady = playWhenReady
        callback.resolve(null)
    }

    @ReactMethod
    fun getPlayWhenReady(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.playWhenReady)
    }

    @ReactMethod
    fun getTrack(index: Int, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        if (index >= 0 && index < musicService.tracks.size) {
            callback.resolve(Arguments.fromBundle(musicService.tracks[index].originalItem))
        } else {
            callback.resolve(null)
        }
    }

    @ReactMethod
    fun getQueue(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(Arguments.fromList(musicService.tracks.map { it.originalItem }))
    }

    @ReactMethod
    fun setQueue(data: ReadableArray?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        try {
            musicService.clear()
            musicService.add(readableArrayToTrackList(data))
            callback.resolve(null)
        } catch (exception: Exception) {
            rejectWithException(callback, exception)
        }
    }

    @ReactMethod
    fun getActiveTrackIndex(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(
            if (musicService.tracks.isEmpty()) null else musicService.getCurrentTrackIndex()
        )
    }

    @ReactMethod
    fun getActiveTrack(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(
            musicService.currentTrack?.let {
                Arguments.fromBundle(it.originalItem)
            }
        )
    }

    @ReactMethod
    fun getDuration(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getDurationInSeconds())
    }

    @ReactMethod
    fun getBufferedPosition(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getBufferedPositionInSeconds())
    }

    @ReactMethod
    fun getPosition(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getPositionInSeconds())
    }

    @ReactMethod
    fun getProgress(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        val bundle = Bundle()
        bundle.putDouble("duration", musicService.getDurationInSeconds());
        bundle.putDouble("position", musicService.getPositionInSeconds());
        bundle.putDouble("buffered", musicService.getBufferedPositionInSeconds());
        callback.resolve(Arguments.fromBundle(bundle))
    }

    @ReactMethod
    fun getPlaybackState(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(Arguments.fromBundle(musicService.getPlayerStateBundle(musicService.state)))
    }

    @ReactMethod
    fun setBrowseTree(mediaItems: ReadableMap, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        val mediaItemsMap = mediaItems.toHashMap()
        musicService.mediaTree = mediaItemsMap.mapValues { readableArrayToMediaItems(it.value as ArrayList<HashMap<String, String>>) }
        Timber.d("refreshing browseTree")
        musicService.notifyChildrenChanged()
        callback.resolve(musicService.mediaTree.toString())
    }

    @ReactMethod
    // this method doesn't seem to affect style after onGetRoot is called, and won't change if notifyChildrenChanged is emitted.
    fun setBrowseTreeStyle(browsableStyle: Int, playableStyle: Int, callback: Promise) = launchInScope {
        fun getStyle(check: Int): Int {
            return when (check) {
                1 -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM
                2 -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_CATEGORY_LIST_ITEM
                3 -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_CATEGORY_GRID_ITEM
                else -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
            }
        }
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        musicService.mediaTreeStyle = listOf(
            getStyle(browsableStyle),
            getStyle(playableStyle)
        )
        callback.resolve(null)
    }

    @ReactMethod
    fun setPlaybackState(mediaID: String, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(null)
    }

    @ReactMethod
    fun acquireWakeLock(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        musicService.acquireWakeLock()
        callback.resolve(null)
    }

    @ReactMethod
    fun abandonWakeLock(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        musicService.abandonWakeLock()
        callback.resolve(null)
    }

    @ReactMethod
    fun validateOnStartCommandIntent(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(musicService.onStartCommandIntentValid)
    }

    // Bridgeless interop layer tries to pass the `Job` from `scope.launch` to the JS side
    // which causes an exception. We can work around this using a wrapper.
    private fun launchInScope(block: suspend () -> Unit) {
        scope.launch {
            block()
        }
    }
}
