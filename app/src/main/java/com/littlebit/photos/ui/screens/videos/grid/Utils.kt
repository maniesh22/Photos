package com.littlebit.photos.ui.screens.videos.grid

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.Formatter.formatFileSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.home.DateWithMarkButton
import com.littlebit.photos.ui.screens.videos.VideoViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoGridList(
    videoViewModel: VideoViewModel,
    scrollState: LazyListState,
    navHostController: NavHostController,
    pullRefreshState: PullRefreshState
) {
    val videoGroups by videoViewModel.videoGroups.collectAsStateWithLifecycle()
    val isLoading by videoViewModel.isLoading.collectAsStateWithLifecycle()

    if (videoGroups.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(PaddingValues(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!isLoading) {
                Column(
                    Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Videos Found",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "You can add Videos to your phone and they will show up here",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        return
    }

    LazyColumn(state = scrollState, modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)) {
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
        items(videoGroups.size) { listIndex ->
            val videoGroup = videoGroups[listIndex]
            VideoGroupSection(videoGroup, videoViewModel, listIndex, navHostController)
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}


@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun VideoGroupSection(
    videoGroup: VideoGroup,
    videoViewModel: VideoViewModel,
    listIndex: Int,
    navHostController: NavHostController
) {
    Column(Modifier.fillMaxWidth()) {
        DateWithMarkButton(date = videoGroup.date) {
            videoViewModel.selectAllVideos(videoGroup, listIndex)
        }
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(Modifier.fillMaxWidth()) {
            val size = videoGroup.videos.size
            for(videoIndex in 0 until size) {
                val video = videoGroup.videos[videoIndex]
                VideoGridItem(
                    video,
                    videoViewModel,
                    listIndex = listIndex,
                    videoIndex = videoIndex
                ) {
                    navHostController.navigate(Screens.VideoScreen.route + "/$videoIndex/$listIndex") {
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoGridItem(
    video: VideoItem,
    videoViewModel: VideoViewModel,
    listIndex: Int,
    videoIndex: Int,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val totalSelectedVideos by videoViewModel.selectedVideos.collectAsStateWithLifecycle()
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
    val size by animateDpAsState(targetValue = if (video.isSelected) 121.dp else 141.dp, label = "", animationSpec = tween(90, 20))
    val animateCornerShape by animateDpAsState(targetValue = if (video.isSelected) 20.dp else 0.dp, label = "", animationSpec = tween(90, 20))
    if (video.uri != null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(141.dp)
                .background(MaterialTheme.colorScheme.surfaceBright)
        ) {
            GlideImage(
                model = video.uri,
                contentDescription = "Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(animateCornerShape))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                if (totalSelectedVideos == 0) {
                                    videoViewModel.selectVideo(video.id, listIndex, videoIndex)
                                    // Trigger haptic feedback on long press
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator?.vibrate(
                                            VibrationEffect.createOneShot(
                                                50, // Duration in milliseconds
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                    } else {
                                        // For older devices
                                        vibrator?.vibrate(50) // Vibrate for 50 milliseconds
                                    }
                                }
                            },
                            onTap = {
                                if (totalSelectedVideos > 0) videoViewModel.selectVideo(
                                    video.id,
                                    listIndex,
                                    videoIndex
                                )
                                else onClick()
                            }
                        )
                    },

            )
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = video.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .width(size - 38.dp)
                )
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = "Play",
                    tint = Color.White,
                )
            }

            Text(
                text = formatFileSize(LocalContext.current, video.size),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )


            val icon = if (video.isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
            val tint = if (totalSelectedVideos > 0) {
                if (video.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            } else Color.Transparent

            Icon(
                imageVector = icon, contentDescription = "Circle", tint = tint, modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
            )

        }
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
}

