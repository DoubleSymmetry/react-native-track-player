@file: OptIn(UnstableApi::class) package com.doublesymmetry.kotlinaudio.service

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.doublesymmetry.kotlinaudio.models.CustomCommandButton
import com.doublesymmetry.kotlinaudio.models.PlayerOptions
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class MusicService : MediaLibraryService() {
    private val binder = MusicBinder()
    lateinit var player: QueuedAudioPlayer
    var mediaLibrarySession: MediaLibrarySession? = null
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    // Create your player and media session in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        player = QueuedAudioPlayer(this, PlayerOptions(nativeExample = true))
        val customCommandButtons =
            CustomCommandButton.entries.map { command -> command.commandButton }
        val callback = object : MediaLibrarySession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val connectionResult = super.onConnect(session, controller)
                val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()

                customCommandButtons.forEach { commandButton ->
                    commandButton.sessionCommand?.let(availableSessionCommands::add)
                }

                return MediaSession.ConnectionResult.accept(
                    availableSessionCommands.build(),
                    connectionResult.availablePlayerCommands
                )
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                val player = player.player;
                when (customCommand.customAction) {
                    CustomCommandButton.JUMP_BACKWARD.customAction -> { player.seekBack() }
                    CustomCommandButton.JUMP_FORWARD.customAction -> { player.seekForward() }
                    CustomCommandButton.NEXT.customAction -> { player.seekToNext() }
                    CustomCommandButton.PREVIOUS.customAction -> { player.seekToPrevious() }
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }
        mediaLibrarySession = MediaLibrarySession.Builder(this, player.exoPlayer, callback)
        .setCustomLayout(customCommandButtons)
            .build()
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.items.isEmpty()) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        player.destroy()
        mediaLibrarySession?.run {
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    inner class MusicBinder : Binder() {
        val service = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        val intentAction = intent?.action
        return if (intentAction != null) {
            super.onBind(intent)
        } else {
            binder
        }
    }
}
