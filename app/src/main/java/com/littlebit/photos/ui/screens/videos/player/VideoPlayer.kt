@file:Suppress("DEPRECATION")

package com.littlebit.photos.ui.screens.videos.player

import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VideoScreen(
        navHostController: NavHostController,
        videoViewModel: VideoViewModel,
        listIndex: Int,
        videoIndex: Int
) {
    val videoList by videoViewModel.videoGroups.collectAsStateWithLifecycle()
    val videoUriList = videoList[listIndex].videos.map { videoItem -> videoItem.uri }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        XVideoPlayerScreen(
                uriList = videoUriList,
                startIndex = videoIndex,
                navHostController = navHostController,
                viewModel = hiltViewModel()
        )
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun XVideoPlayerScreen(
        uriList: List<Uri?>,
        startIndex: Int,
        navHostController: NavHostController,
        viewModel: VideoPlayerViewModel,
) {
    var backHandlerUsed by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    val lifecycleController = LocalLifecycleOwner.current.lifecycle
    val playerView = remember { PlayerView(context) }
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

    AndroidView(
            modifier =
                    Modifier.fillMaxSize().background(Color.Black).focusable().onKeyEvent { event ->
                        playerView.dispatchKeyEvent(event.nativeKeyEvent)
                    },
            factory = {
                playerView.apply {
                    setShowRewindButton(false)
                    setShowFastForwardButton(false)
                    setShowSubtitleButton(true)
                    setControllerVisibilityListener(
                            PlayerView.ControllerVisibilityListener {
                                systemUiController.isSystemBarsVisible = it == 0
                            }
                    )
                }
            }
    )

    BackHandler {
        backHandlerUsed = true
        scope.launch {
            delay(300)
            navHostController.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        val lifecycleObserver =
                object : DefaultLifecycleObserver {
                    override fun onPause(owner: LifecycleOwner) {
                        // Pause the player when the app goes into the background
                        viewModel.pause()
                    }

                    override fun onResume(owner: LifecycleOwner) {
                        // Resume the player when the app returns to the foreground
                        viewModel.resume()
                    }
                }
        lifecycleController.addObserver(lifecycleObserver)
        onDispose {
            if (backHandlerUsed) {
                viewModel.releasePlayer()
            } else {
                viewModel.safePlayBackPosition()
            }
            lifecycleController.removeObserver(lifecycleObserver)
        }
    }

    LaunchedEffect(playbackState) {
        if (viewModel.isPlayerNull()) {
            viewModel.initialisePlayer(context)
            viewModel.setPlayer(playerView)
            viewModel.setMediaItems(uriList, startIndex, 0)
            viewModel.prepare()
            viewModel.setPlayWhenReady(true)
        } else {
            viewModel.setPlayer(playerView)
            systemUiController.isSystemBarsVisible = false
        }
    }
}
