package com.littlebit.photos.ui.screens.videos.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.littlebit.photos.ui.screens.audio.player.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@HiltViewModel
class VideoPlayerViewModel @Inject constructor() : ViewModel() {
    private val _playbackState: MutableStateFlow<PlaybackState> =
            MutableStateFlow(PlaybackState.IDLE)
    private val _playbackPosition: MutableStateFlow<Long> = MutableStateFlow(0L)
    private val player: MutableStateFlow<Player?> = MutableStateFlow(null)
    val playbackState = _playbackState

    fun initialisePlayer(context: Context) {
        player.value = ExoPlayer.Builder(context).build()
    }

    fun releasePlayer() {
        try {
            player.value?.release()
            _playbackState.value = PlaybackState.IDLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPlayWhenReady(playWhenReady: Boolean) {
        try {
            player.value?.playWhenReady = playWhenReady
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMediaItems(uris: List<Uri?>, startIndex: Int, playbackPosition: Long) {
        try {
            player.value?.setMediaItems(
                    uris.map { uri -> MediaItem.fromUri(uri!!) },
                    startIndex,
                    playbackPosition
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.value?.release()
        Log.d("CLEARED", "onCleared:")
    }

    fun setPlayer(playerView: PlayerView) {
        playerView.player = player.value
    }

    fun prepare() {
        player.value?.prepare()
    }

    fun safePlayBackPosition() {
        _playbackPosition.value = player.value?.currentPosition!!
        Log.d("SAVED", "safePlayBackPosition: ${_playbackPosition.value}")
    }

    fun isPlayerNull(): Boolean {
        return player.value == null
    }

    fun resume() {
        try {
            if (player.value != null) {
                player.value?.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            if (player.value != null) {
                player.value?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
