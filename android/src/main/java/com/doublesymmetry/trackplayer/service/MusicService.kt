package com.doublesymmetry.trackplayer.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.KeyEvent
import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.media.utils.MediaConstants
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.LibraryResult
import androidx.media3.common.MediaItem
import androidx.media3.common.Rating
import androidx.media3.common.util.BitmapLoader
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import com.doublesymmetry.kotlinaudio.models.*
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.doublesymmetry.trackplayer.HeadlessJsMediaService
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toMilliseconds
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toSeconds
import com.doublesymmetry.trackplayer.extensions.asLibState
import com.doublesymmetry.trackplayer.extensions.find
import com.doublesymmetry.trackplayer.model.MetadataAdapter
import com.doublesymmetry.trackplayer.model.PlaybackMetadata
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.model.TrackAudioItem
import com.doublesymmetry.trackplayer.module.MusicEvents
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.METADATA_PAYLOAD_KEY
import com.doublesymmetry.trackplayer.utils.BundleUtils
import com.doublesymmetry.trackplayer.utils.BundleUtils.setRating
import com.doublesymmetry.trackplayer.utils.CoilBitmapLoader
import com.doublesymmetry.trackplayer.utils.buildMediaItem
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@OptIn(UnstableApi::class)
@MainThread
class MusicService : HeadlessJsMediaService() {
    private lateinit var player: QueuedAudioPlayer
    private val binder = MusicBinder()
    private val scope = MainScope()
    private lateinit var fakePlayer: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession
    private var progressUpdateJob: Job? = null
    var mediaTree: Map<String, List<MediaItem>> = HashMap()
    var mediaTreeStyle: List<Int> = listOf(
        MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM,
        MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
    )
    private var sessionCommands: SessionCommands? = null
    private var playerCommands: Player.Commands? = null
    private var customLayout: List<CommandButton> = listOf()
    private var lastWake: Long = 0
    var onStartCommandIntentValid: Boolean = true

    fun acquireWakeLock() {
        acquireWakeLockNow(this)
    }

    fun abandonWakeLock() {
        sWakeLock?.release()
    }

    fun getBitmapLoader(): BitmapLoader {
        return mediaSession.bitmapLoader
    }

    fun getCurrentBitmap(): ListenableFuture<Bitmap>? {
        return player.exoPlayer.currentMediaItem?.mediaMetadata?.let {
            mediaSession.bitmapLoader.loadBitmapFromMetadata(
                it
            )
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return "RNTP-${element.className}:${element.methodName}"
            }
        })
        fakePlayer = ExoPlayer.Builder(this).build()
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Add the Uri data so apps can identify that it was a notification click
            data = Uri.parse("trackplayer://notification.click")
            action = Intent.ACTION_VIEW
        }
        mediaSession = MediaLibrarySession.Builder(this, fakePlayer,
            InnerMediaSessionCallback()
        )
            .setBitmapLoader(CacheBitmapLoader(CoilBitmapLoader(this)))
            // https://github.com/androidx/media/issues/1218
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    openAppIntent,
                    getPendingIntentFlags()
                )
            )
            .build()
        super.onCreate()
    }

    enum class AppKilledPlaybackBehavior(val string: String) {
        CONTINUE_PLAYBACK("continue-playback"),
        PAUSE_PLAYBACK("pause-playback"),
        STOP_PLAYBACK_AND_REMOVE_NOTIFICATION("stop-playback-and-remove-notification")
    }

    private var appKilledPlaybackBehavior =
        AppKilledPlaybackBehavior.STOP_PLAYBACK_AND_REMOVE_NOTIFICATION
    private var stopForegroundGracePeriod: Int = DEFAULT_STOP_FOREGROUND_GRACE_PERIOD

    val tracks: List<Track>
        get() = player.items.map { (it as TrackAudioItem).track }

    val currentTrack: Track?
        get() {
            return (player.currentItem as TrackAudioItem?)?.track
        }

    val state
        get() = player.playerState

    var ratingType: Int
        get() = player.ratingType
        set(value) {
            player.ratingType = value
        }

    val playbackError
        get() = player.playbackError

    val event
        get() = player.playerEventHolder

    var playWhenReady: Boolean
        get() = player.playWhenReady
        set(value) {
            player.playWhenReady = value
        }

    private var latestOptions: Bundle? = null
    private var commandStarted = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onStartCommandIntentValid = intent != null
        Timber.d("onStartCommand: ${intent?.action}, ${intent?.`package`}")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // HACK: this is not supposed to be here. I definitely screwed up. but Why?
            onMediaKeyEvent(intent)
        }
        // HACK: Why is onPlay triggering onStartCommand??
        if (!commandStarted) {
            commandStarted = true
            super.onStartCommand(intent, flags, startId)
        }
        return START_STICKY
    }

    @MainThread
    fun setupPlayer(playerOptions: Bundle?) {
        if (this::player.isInitialized) {
            print("Player was initialized previously. Preventing reinitialization.")
            return
        }
        Timber.d("Setting up player")
        val options = PlayerOptions(
            alwaysShowNext = playerOptions?.getBoolean(ALWAYS_SHOW_NEXT, true) ?: true,
            audioContentType = when (playerOptions?.getString(ANDROID_AUDIO_CONTENT_TYPE)) {
                "music" -> C.AUDIO_CONTENT_TYPE_MUSIC
                "speech" -> C.AUDIO_CONTENT_TYPE_SPEECH
                "sonification" -> C.AUDIO_CONTENT_TYPE_SONIFICATION
                "movie" -> C.AUDIO_CONTENT_TYPE_MOVIE
                "unknown" -> C.AUDIO_CONTENT_TYPE_UNKNOWN
                else -> C.AUDIO_CONTENT_TYPE_MUSIC
            },
            bufferOptions = BufferOptions(
                playerOptions?.getDouble(MIN_BUFFER_KEY)?.toMilliseconds()?.toInt(),
                playerOptions?.getDouble(MAX_BUFFER_KEY)?.toMilliseconds()?.toInt(),
                playerOptions?.getDouble(PLAY_BUFFER_KEY)?.toMilliseconds()?.toInt(),
                playerOptions?.getDouble(BACK_BUFFER_KEY)?.toMilliseconds()?.toInt(),
            ),
            cacheSize = playerOptions?.getDouble(MAX_CACHE_SIZE_KEY)?.toLong() ?: 0,
            handleAudioBecomingNoisy = playerOptions?.getBoolean(HANDLE_NOISY, true) ?: true,
            handleAudioFocus = playerOptions?.getBoolean(AUTO_HANDLE_INTERRUPTIONS) ?: true,
            interceptPlayerActionsTriggeredExternally = true,
            skipSilence = playerOptions?.getBoolean(SKIP_SILENCE) ?: false,
            wakeMode = playerOptions?.getInt(WAKE_MODE, 0) ?: 0
        )
        player = QueuedAudioPlayer(this@MusicService, options)
        fakePlayer.release()
        mediaSession.player = player.exoPlayer
        observeEvents()
    }

    @MainThread
    fun updateOptions(options: Bundle) {
        latestOptions = options
        val androidOptions = options.getBundle(ANDROID_OPTIONS_KEY)

        if (androidOptions?.containsKey(AUDIO_OFFLOAD_KEY) == true) {
            player.setAudioOffload(androidOptions.getBoolean(AUDIO_OFFLOAD_KEY))
        }
        if (androidOptions?.containsKey(SKIP_SILENCE) == true) {
            player.skipSilence = androidOptions.getBoolean(SKIP_SILENCE)
        }

        appKilledPlaybackBehavior =
            AppKilledPlaybackBehavior::string.find(
                androidOptions?.getString(
                    APP_KILLED_PLAYBACK_BEHAVIOR_KEY
                )
            ) ?: AppKilledPlaybackBehavior.CONTINUE_PLAYBACK

        BundleUtils.getIntOrNull(androidOptions, STOP_FOREGROUND_GRACE_PERIOD_KEY)
            ?.let { stopForegroundGracePeriod = it }

        player.alwaysPauseOnInterruption =
            androidOptions?.getBoolean(PAUSE_ON_INTERRUPTION_KEY) ?: false
        player.shuffleMode = androidOptions?.getBoolean(SHUFFLE_KEY) ?: false

        // setup progress update events if configured
        progressUpdateJob?.cancel()
        val updateInterval =
            BundleUtils.getDoubleOrNull(options, PROGRESS_UPDATE_EVENT_INTERVAL_KEY)
        if (updateInterval != null && updateInterval > 0) {
            progressUpdateJob = scope.launch {
                progressUpdateEventFlow(updateInterval).collect {
                    emit(
                        MusicEvents.PLAYBACK_PROGRESS_UPDATED,
                        it
                    )
                }
            }
        }
        val capabilities =
            options.getIntegerArrayList("capabilities")?.map { Capability.entries[it] }
                ?: emptyList()
        var notificationCapabilities = (
                options.getIntegerArrayList("notificationCapabilities")
                    ?: options.getIntegerArrayList("compactCapabilities"))
            ?.map { Capability.entries[it] } ?: emptyList()
        if (notificationCapabilities.isEmpty()) notificationCapabilities = capabilities

        val customActions = options.getBundle(CUSTOM_ACTIONS_KEY)

        val playerCommandsBuilder = Player.Commands.Builder().addAll(
            // HACK: without COMMAND_GET_CURRENT_MEDIA_ITEM, notification cannot be created
            Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
            Player.COMMAND_GET_TRACKS,
            Player.COMMAND_GET_TIMELINE,
            Player.COMMAND_GET_METADATA,
            Player.COMMAND_GET_AUDIO_ATTRIBUTES,
            Player.COMMAND_GET_VOLUME,
            Player.COMMAND_GET_DEVICE_VOLUME,
            Player.COMMAND_GET_TEXT,
            Player.COMMAND_SEEK_TO_MEDIA_ITEM,
            Player.COMMAND_SET_MEDIA_ITEM,
            Player.COMMAND_PREPARE,
            Player.COMMAND_RELEASE,
        )
        notificationCapabilities.forEach {
            when (it) {
                Capability.PLAY, Capability.PAUSE -> {
                    playerCommandsBuilder.add(Player.COMMAND_PLAY_PAUSE)
                }

                Capability.STOP -> {
                    playerCommandsBuilder.add(Player.COMMAND_STOP)
                }

                Capability.SEEK_TO -> {
                    playerCommandsBuilder.add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                }

                else -> {}
            }
        }
        customLayout = CustomCommandButton.entries
            .filter { notificationCapabilities.contains(it.capability) }
            .map { c -> c.commandButton }
        val sessionCommandsBuilder =
            MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
        customLayout.forEach { v ->
            v.sessionCommand?.let { sessionCommandsBuilder.add(it) }
        }

        sessionCommands = sessionCommandsBuilder.build()
        playerCommands = playerCommandsBuilder.build()

        if (mediaSession.mediaNotificationControllerInfo != null) {
            // https://github.com/androidx/media/blob/c35a9d62baec57118ea898e271ac66819399649b/demos/session_service/src/main/java/androidx/media3/demo/session/DemoMediaLibrarySessionCallback.kt#L107
            mediaSession.setCustomLayout(
                mediaSession.mediaNotificationControllerInfo!!,
                customLayout
            )
            mediaSession.setAvailableCommands(
                mediaSession.mediaNotificationControllerInfo!!,
                sessionCommandsBuilder.build(),
                playerCommands!!
            )
        }
    }

    @MainThread
    private fun progressUpdateEventFlow(interval: Double) = flow {
        while (true) {
            if (player.isPlaying) {
                val bundle = progressUpdateEvent()
                emit(bundle)
            }

            delay((interval * 1000).toLong())
        }
    }

    @MainThread
    private suspend fun progressUpdateEvent(): Bundle {
        return withContext(Dispatchers.Main) {
            Bundle().apply {
                putDouble(POSITION_KEY, player.position.toSeconds())
                putDouble(DURATION_KEY, player.duration.toSeconds())
                putDouble(BUFFERED_POSITION_KEY, player.bufferedPosition.toSeconds())
                putInt(TRACK_KEY, player.currentIndex)
            }
        }
    }

    @MainThread
    fun add(track: Track) {
        add(listOf(track))
    }

    @MainThread
    fun add(tracks: List<Track>) {
        val items = tracks.map { it.toAudioItem() }
        player.add(items)
    }

    @MainThread
    fun add(tracks: List<Track>, atIndex: Int) {
        val items = tracks.map { it.toAudioItem() }
        player.add(items, atIndex)
    }

    @MainThread
    fun load(track: Track) {
        player.load(track.toAudioItem())
    }

    @MainThread
    fun move(fromIndex: Int, toIndex: Int) {
        player.move(fromIndex, toIndex);
    }

    @MainThread
    fun remove(index: Int) {
        remove(listOf(index))
    }

    @MainThread
    fun remove(indexes: List<Int>) {
        player.remove(indexes)
    }

    @MainThread
    fun clear() {
        player.clear()
    }

    @MainThread
    fun play() {
        player.play()
    }

    @MainThread
    fun pause() {
        player.pause()
    }

    @MainThread
    fun stop() {
        player.stop()
    }

    @MainThread
    fun removeUpcomingTracks() {
        player.removeUpcomingItems()
    }

    @MainThread
    fun removePreviousTracks() {
        player.removePreviousItems()
    }

    @MainThread
    fun skip(index: Int) {
        player.jumpToItem(index)
    }

    @MainThread
    fun skipToNext() {
        player.next()
    }

    @MainThread
    fun skipToPrevious() {
        player.previous()
    }

    @MainThread
    fun seekTo(seconds: Float) {
        player.seek((seconds * 1000).toLong(), TimeUnit.MILLISECONDS)
    }

    @MainThread
    fun seekBy(offset: Float) {
        player.seekBy((offset.toLong()), TimeUnit.SECONDS)
    }

    @MainThread
    fun retry() {
        player.prepare()
    }

    @MainThread
    fun getCurrentTrackIndex(): Int = player.currentIndex

    @MainThread
    fun getRate(): Float = player.playbackSpeed

    @MainThread
    fun setRate(value: Float) {
        player.playbackSpeed = value
    }

    @MainThread
    fun getRepeatMode(): RepeatMode = player.repeatMode

    @MainThread
    fun setRepeatMode(value: RepeatMode) {
        player.repeatMode = value
    }

    @MainThread
    fun getVolume(): Float = player.volume

    @MainThread
    fun setVolume(value: Float) {
        player.volume = value
    }

    @MainThread
    fun getDurationInSeconds(): Double = player.duration.toSeconds()

    @MainThread
    fun getPositionInSeconds(): Double = player.position.toSeconds()

    @MainThread
    fun getBufferedPositionInSeconds(): Double = player.bufferedPosition.toSeconds()

    @MainThread
    fun getPlayerStateBundle(state: AudioPlayerState): Bundle {
        val bundle = Bundle()
        bundle.putString(STATE_KEY, state.asLibState.state)
        if (state == AudioPlayerState.ERROR) {
            bundle.putBundle(ERROR_KEY, getPlaybackErrorBundle())
        }
        return bundle
    }

    @MainThread
    fun updateMetadataForTrack(index: Int, bundle: Bundle) {
        tracks[index].let { currentTrack ->
            currentTrack.setMetadata(reactContext, bundle, 0)

            player.replaceItem(index, currentTrack.toAudioItem())
        }
    }

    @MainThread
    fun updateNowPlayingMetadata(bundle: Bundle) {
        updateMetadataForTrack(player.currentIndex, bundle)
    }

    private fun emitPlaybackTrackChangedEvents(
        previousIndex: Int?,
        oldPosition: Double
    ) {
        val bundle = Bundle()
        bundle.putDouble("lastPosition", oldPosition)
        if (tracks.isNotEmpty()) {
            bundle.putInt("index", player.currentIndex)
            bundle.putBundle("track", tracks[player.currentIndex].originalItem)
            if (previousIndex != null) {
                bundle.putInt("lastIndex", previousIndex)
                bundle.putBundle("lastTrack", tracks[previousIndex].originalItem)
            }
        }
        emit(MusicEvents.PLAYBACK_ACTIVE_TRACK_CHANGED, bundle)
    }

    private fun emitQueueEndedEvent() {
        val bundle = Bundle()
        bundle.putInt(TRACK_KEY, player.currentIndex)
        bundle.putDouble(POSITION_KEY, player.position.toSeconds())
        emit(MusicEvents.PLAYBACK_QUEUE_ENDED, bundle)
    }

    @MainThread
    private fun observeEvents() {
        scope.launch {
            event.stateChange.collect {
                emit(MusicEvents.PLAYBACK_STATE, getPlayerStateBundle(it))

                if (it == AudioPlayerState.ENDED && player.nextItem == null) {
                    emitQueueEndedEvent()
                }
            }
        }

        scope.launch {
            event.audioItemTransition.collect {
                if (it !is AudioItemTransitionReason.REPEAT) {
                    emitPlaybackTrackChangedEvents(
                        player.previousIndex,
                        (it?.oldPosition ?: 0).toSeconds()
                    )
                }
            }
        }

        scope.launch {
            event.onAudioFocusChanged.collect {
                Bundle().apply {
                    putBoolean(IS_FOCUS_LOSS_PERMANENT_KEY, it.isFocusLostPermanently)
                    putBoolean(IS_PAUSED_KEY, it.isPaused)
                    emit(MusicEvents.BUTTON_DUCK, this)
                }
            }
        }

        scope.launch {
            event.onPlayerActionTriggeredExternally.collect {
                when (it) {
                    is MediaSessionCallback.RATING -> {
                        Bundle().apply {
                            setRating(this, "rating", it.rating)
                            emit(MusicEvents.BUTTON_SET_RATING, this)
                        }
                    }

                    is MediaSessionCallback.SEEK -> {
                        Bundle().apply {
                            putDouble("position", it.positionMs.toSeconds())
                            emit(MusicEvents.BUTTON_SEEK_TO, this)
                        }
                    }

                    MediaSessionCallback.PLAY -> emit(MusicEvents.BUTTON_PLAY)
                    MediaSessionCallback.PAUSE -> emit(MusicEvents.BUTTON_PAUSE)
                    MediaSessionCallback.NEXT -> emit(MusicEvents.BUTTON_SKIP_NEXT)
                    MediaSessionCallback.PREVIOUS -> emit(MusicEvents.BUTTON_SKIP_PREVIOUS)
                    MediaSessionCallback.STOP -> emit(MusicEvents.BUTTON_STOP)
                    MediaSessionCallback.FORWARD -> {
                        Bundle().apply {
                            val interval = latestOptions?.getDouble(
                                FORWARD_JUMP_INTERVAL_KEY,
                                DEFAULT_JUMP_INTERVAL
                            ) ?: DEFAULT_JUMP_INTERVAL
                            putInt("interval", interval.toInt())
                            emit(MusicEvents.BUTTON_JUMP_FORWARD, this)
                        }
                    }

                    MediaSessionCallback.REWIND -> {
                        Bundle().apply {
                            val interval = latestOptions?.getDouble(
                                BACKWARD_JUMP_INTERVAL_KEY,
                                DEFAULT_JUMP_INTERVAL
                            ) ?: DEFAULT_JUMP_INTERVAL
                            putInt("interval", interval.toInt())
                            emit(MusicEvents.BUTTON_JUMP_BACKWARD, this)
                        }
                    }
                }
            }
        }

        scope.launch {
            event.onTimedMetadata.collect {
                val data = MetadataAdapter.fromMetadata(it)
                val bundle = Bundle().apply {
                    putParcelableArrayList(METADATA_PAYLOAD_KEY, ArrayList(data))
                }
                emit(MusicEvents.METADATA_TIMED_RECEIVED, bundle)

                // TODO: Handle the different types of metadata and publish to new events
                val metadata = PlaybackMetadata.fromId3Metadata(it)
                    ?: PlaybackMetadata.fromIcy(it)
                    ?: PlaybackMetadata.fromVorbisComment(it)
                    ?: PlaybackMetadata.fromQuickTime(it)

                if (metadata != null) {
                    Bundle().apply {
                        putString("source", metadata.source)
                        putString("title", metadata.title)
                        putString("url", metadata.url)
                        putString("artist", metadata.artist)
                        putString("album", metadata.album)
                        putString("date", metadata.date)
                        putString("genre", metadata.genre)
                        emit(MusicEvents.PLAYBACK_METADATA, this)
                    }
                }
            }
        }

        scope.launch {
            event.onCommonMetadata.collect {
                val data = MetadataAdapter.fromMediaMetadata(it)
                val bundle = Bundle().apply {
                    putBundle(METADATA_PAYLOAD_KEY, data)
                }
                emit(MusicEvents.METADATA_COMMON_RECEIVED, bundle)
            }
        }

        scope.launch {
            event.playWhenReadyChange.collect {
                Bundle().apply {
                    putBoolean("playWhenReady", it.playWhenReady)
                    emit(MusicEvents.PLAYBACK_PLAY_WHEN_READY_CHANGED, this)
                }
            }
        }

        scope.launch {
            event.playbackError.collect {
                emit(MusicEvents.PLAYBACK_ERROR, getPlaybackErrorBundle())
            }
        }
    }

    private fun getPlaybackErrorBundle(): Bundle {
        val bundle = Bundle()
        val error = playbackError
        if (error?.message != null) {
            bundle.putString("message", error.message)
        }
        if (error?.code != null) {
            bundle.putString("code", "android-" + error.code)
        }
        return bundle
    }

    @SuppressLint("VisibleForTests")
    @MainThread
    fun emit(event: String, data: Bundle? = null) {
        reactContext?.emitDeviceEvent(event, data?.let { Arguments.fromBundle(it) })
    }

    @SuppressLint("VisibleForTests")
    @MainThread
    private fun emitList(event: String, data: List<Bundle> = emptyList()) {
        val payload = Arguments.createArray()
        data.forEach { payload.pushMap(Arguments.fromBundle(it)) }

        reactContext?.emitDeviceEvent(event, payload)
    }

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig {
        return HeadlessJsTaskConfig(TASK_KEY, Arguments.createMap(), 0, true)
    }

    @MainThread
    override fun onBind(intent: Intent?): IBinder? {
        val intentAction = intent?.action
        Timber.d("intentAction = $intentAction")
        return if (intentAction != null) {
            super.onBind(intent)
        } else {
            binder
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        val intentAction = intent?.action
        Timber.d("intentAction = $intentAction")
        return super.onUnbind(intent)
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        // https://github.com/androidx/media/issues/843#issuecomment-1860555950
        super.onUpdateNotification(session, true)
    }

    @MainThread
    override fun onTaskRemoved(rootIntent: Intent?) {
        onUnbind(rootIntent)
        Timber.d("isInitialized = ${::player.isInitialized}, appKilledPlaybackBehavior = $appKilledPlaybackBehavior")
        if (!::player.isInitialized) {
            mediaSession.release()
            return
        }

        when (appKilledPlaybackBehavior) {
            AppKilledPlaybackBehavior.PAUSE_PLAYBACK -> {
                Timber.d("Pausing playback - appKilledPlaybackBehavior = $appKilledPlaybackBehavior")
                player.pause()
            }
            AppKilledPlaybackBehavior.STOP_PLAYBACK_AND_REMOVE_NOTIFICATION -> {
                Timber.d("Killing service - appKilledPlaybackBehavior = $appKilledPlaybackBehavior")
                mediaSession.release()
                player.clear()
                player.stop()
                // HACK: the service first stops, then starts, then call onTaskRemove. Why system
                // registers the service being restarted?
                player.destroy()
                scope.cancel()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                onDestroy()
                // https://github.com/androidx/media/issues/27#issuecomment-1456042326
                stopSelf()
                exitProcess(0)
            }

            else -> {}
        }
    }

    @SuppressLint("VisibleForTests")
    private fun selfWake(clientPackageName: String): Boolean {
        val reactActivity = reactContext?.currentActivity
        if (
        // HACK: validate reactActivity is present; if not, send wake intent
            (reactActivity == null || reactActivity.isDestroyed)
            && Settings.canDrawOverlays(this)
        ) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastWake < 100000) {
                return false
            }
            lastWake = currentTime
            val activityIntent = packageManager.getLaunchIntentForPackage(packageName)
            activityIntent!!.data = Uri.parse("trackplayer://service-bound")
            activityIntent.action = Intent.ACTION_VIEW
            activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            var activityOptions = ActivityOptions.makeBasic()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activityOptions = activityOptions.setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }
            this.startActivity(activityIntent, activityOptions.toBundle())
            return true
        }
        return false
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        Timber.d("${controllerInfo.packageName}")
        return mediaSession
    }

    fun notifyChildrenChanged() {
        mediaSession.connectedControllers.forEach { controller ->
            mediaTree.forEach { it ->
                mediaSession.notifyChildrenChanged(controller, it.key, it.value.size, null)
            }

        }
    }

    @MainThread
    override fun onHeadlessJsTaskFinish(taskId: Int) {
        // This is empty so ReactNative doesn't kill this service
    }

    @MainThread
    override fun onDestroy() {
        if (::player.isInitialized) {
            Timber.d("Releasing media session and destroying player")
            mediaSession.release()
            player.destroy()
        }

        progressUpdateJob?.cancel()
        super.onDestroy()
    }

    fun onMediaKeyEvent(intent: Intent?): Boolean? {
        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            intent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        }

        if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
            return when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    emit(MusicEvents.BUTTON_PLAY_PAUSE)
                    true
                }

                KeyEvent.KEYCODE_MEDIA_STOP -> {
                    emit(MusicEvents.BUTTON_STOP)
                    true
                }

                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    emit(MusicEvents.BUTTON_PAUSE)
                    true
                }

                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    emit(MusicEvents.BUTTON_PLAY)
                    true
                }

                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    emit(MusicEvents.BUTTON_SKIP_NEXT)
                    true
                }

                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    emit(MusicEvents.BUTTON_SKIP_PREVIOUS)
                    true
                }

                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD, KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                    emit(MusicEvents.BUTTON_JUMP_FORWARD)
                    true
                }

                KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD, KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                    emit(MusicEvents.BUTTON_JUMP_BACKWARD)
                    true
                }

                else -> null
            }
        }
        return null
    }

    @MainThread
    inner class MusicBinder : Binder() {
        val service = this@MusicService
    }

    private inner class InnerMediaSessionCallback : MediaLibrarySession.Callback {
        // HACK: I'm sure most of the callbacks were not implemented correctly.
        // ATM I only care that andorid auto still functions.

        private val rootItem =
            buildMediaItem(title = "root", mediaId = AA_ROOT_KEY, isPlayable = false)
        private val forYouItem =
            buildMediaItem(title = "For You", mediaId = AA_FOR_YOU_KEY, isPlayable = false)

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            emit(MusicEvents.CONNECTOR_DISCONNECTED, Bundle().apply {
                putString("package", controller.packageName)
            })
            super.onDisconnected(session, controller)
        }

        // Configure commands available to the controller in onConnect()
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Timber.d("${controller.packageName}")
            val isMediaNotificationController = session.isMediaNotificationController(controller)
            val isAutomotiveController = session.isAutomotiveController(controller)
            val isAutoCompanionController = session.isAutoCompanionController(controller)
            emit(MusicEvents.CONNECTOR_CONNECTED, Bundle().apply {
                putString("package", controller.packageName)
                putBoolean("isMediaNotificationController", isMediaNotificationController)
                putBoolean("isAutomotiveController", isAutomotiveController)
                putBoolean("isAutoCompanionController", isAutoCompanionController)
            })
            if (controller.packageName in arrayOf(
                    "com.android.systemui",
                    // https://github.com/googlesamples/android-media-controller
                    "com.example.android.mediacontroller",
                    // Android Auto
                    "com.google.android.projection.gearhead"
                )
            ) {
                // HACK: attempt to wake up activity (for legacy APM). if not, start headless.
                if (!selfWake(controller.packageName)) {
                    onStartCommand(null, 0, 0)
                }
            }
            return if (
                isMediaNotificationController ||
                isAutomotiveController ||
                isAutoCompanionController
            ) {
                MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setCustomLayout(customLayout)
                    .setAvailableSessionCommands(
                        sessionCommands
                            ?: MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
                    )
                    .setAvailablePlayerCommands(
                        playerCommands ?: MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                    )
                    .build()
            } else {
                super.onConnect(session, controller)
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            command: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            player.forwardingPlayer.let {
                when (command.customAction) {
                    CustomCommandButton.JUMP_BACKWARD.customAction -> { it.seekBack() }
                    CustomCommandButton.JUMP_FORWARD.customAction -> { it.seekForward() }
                    CustomCommandButton.NEXT.customAction -> { it.seekToNext() }
                    CustomCommandButton.PREVIOUS.customAction -> { it.seekToPrevious() }
                }
            }
            return super.onCustomCommand(session, controller, command, args)
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Timber.d("${browser.packageName}")
            val rootExtras = Bundle().apply {
                putBoolean("android.media.browse.CONTENT_STYLE_SUPPORTED", true)
                putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", mediaTreeStyle[0])
                putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", mediaTreeStyle[1])
            }
            val libraryParams = LibraryParams.Builder().setExtras(rootExtras).build()
            // https://github.com/androidx/media/issues/1731#issuecomment-2411109462
            val mRootItem = when (browser.packageName) {
                "com.google.android.googlequicksearchbox" -> {
                    if (mediaTree[AA_FOR_YOU_KEY] == null) rootItem else forYouItem
                }

                else -> rootItem
            }
            return Futures.immediateFuture(LibraryResult.ofItem(mRootItem, libraryParams))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            emit(MusicEvents.BUTTON_BROWSE, Bundle().apply { putString("mediaId", parentId) });
            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    mediaTree[parentId] ?: listOf(),
                    null
                )
            )
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Timber.d("${browser.packageName}, mediaId = $mediaId")
            // emit(MusicEvents.BUTTON_PLAY_FROM_ID, Bundle().apply { putString("id", mediaId) })
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, null))
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            Timber.d("${browser.packageName}, query = $query")
            return super.onSearch(session, browser, query, params)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            Timber.d("${controller.packageName}, ${mediaItems[0].mediaId}, ${mediaItems.size}")
            return super.onAddMediaItems(mediaSession, controller, mediaItems)
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Timber.d("${controller.packageName}, ${mediaItems[0].toBundle()}")
            if (mediaItems[0].requestMetadata.searchQuery == null) {
                emit(MusicEvents.BUTTON_PLAY_FROM_ID, Bundle().apply {
                    putString("id", mediaItems[0].mediaId)
                })
            } else {
                emit(MusicEvents.BUTTON_PLAY_FROM_SEARCH, Bundle().apply {
                    putString("query", mediaItems[0].requestMetadata.searchQuery)
                })
            }
            return super.onSetMediaItems(
                mediaSession,
                controller,
                mediaItems,
                startIndex,
                startPositionMs
            )
        }

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            return onMediaKeyEvent(intent) ?: super.onMediaButtonEvent(
                session,
                controllerInfo,
                intent
            )
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            Timber.d("${browser.packageName}, $query")
            return super.onGetSearchResult(session, browser, query, page, pageSize, params)
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            emit(MusicEvents.PLAYBACK_RESUME, Bundle().apply {
                putString("package", controller.packageName)
            })
            return super.onPlaybackResumption(mediaSession, controller)
        }

        override fun onSetRating(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            rating: Rating
        ): ListenableFuture<SessionResult> {
            Bundle().apply {
                setRating(this, "rating", rating)
                emit(MusicEvents.BUTTON_SET_RATING, this)
            }
            return super.onSetRating(session, controller, rating)
        }
    }

    private fun getPendingIntentFlags(): Int {
        return PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    }

    companion object {
        const val EMPTY_NOTIFICATION_ID = 1
        const val STATE_KEY = "state"
        const val ERROR_KEY = "error"
        const val EVENT_KEY = "event"
        const val DATA_KEY = "data"
        const val TRACK_KEY = "track"
        const val NEXT_TRACK_KEY = "nextTrack"
        const val POSITION_KEY = "position"
        const val DURATION_KEY = "duration"
        const val BUFFERED_POSITION_KEY = "buffered"

        const val TASK_KEY = "TrackPlayer"

        const val MIN_BUFFER_KEY = "minBuffer"
        const val MAX_BUFFER_KEY = "maxBuffer"
        const val PLAY_BUFFER_KEY = "playBuffer"
        const val BACK_BUFFER_KEY = "backBuffer"

        const val FORWARD_JUMP_INTERVAL_KEY = "forwardJumpInterval"
        const val BACKWARD_JUMP_INTERVAL_KEY = "backwardJumpInterval"
        const val PROGRESS_UPDATE_EVENT_INTERVAL_KEY = "progressUpdateEventInterval"

        const val MAX_CACHE_SIZE_KEY = "maxCacheSize"

        const val ANDROID_OPTIONS_KEY = "android"

        const val CUSTOM_ACTIONS_KEY = "customActions"

        const val APP_KILLED_PLAYBACK_BEHAVIOR_KEY = "appKilledPlaybackBehavior"
        const val AUDIO_OFFLOAD_KEY = "audioOffload"
        const val SHUFFLE_KEY = "shuffle"
        const val STOP_FOREGROUND_GRACE_PERIOD_KEY = "stopForegroundGracePeriod"
        const val PAUSE_ON_INTERRUPTION_KEY = "alwaysPauseOnInterruption"
        const val AUTO_UPDATE_METADATA = "autoUpdateMetadata"
        const val AUTO_HANDLE_INTERRUPTIONS = "autoHandleInterruptions"
        const val ANDROID_AUDIO_CONTENT_TYPE = "androidAudioContentType"
        const val IS_FOCUS_LOSS_PERMANENT_KEY = "permanent"
        const val IS_PAUSED_KEY = "paused"

        const val HANDLE_NOISY = "androidHandleAudioBecomingNoisy"
        const val CROSSFADE = "crossfade"
        const val ALWAYS_SHOW_NEXT = "androidAlwaysShowNext"
        const val SKIP_SILENCE = "androidSkipSilence"
        const val WAKE_MODE = "androidWakeMode"

        const val AA_FOR_YOU_KEY = "for-you"
        const val AA_ROOT_KEY = "/"

        const val DEFAULT_JUMP_INTERVAL = 15.0
        const val DEFAULT_STOP_FOREGROUND_GRACE_PERIOD = 5
    }
}
