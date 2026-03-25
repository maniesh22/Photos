@file:Suppress("DEPRECATION")

package com.littlebit.photos.ui.screens.audio.player

import android.content.ContentUris
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOn
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.ShuffleOn
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.audio.FileInfo
import com.littlebit.photos.ui.screens.videos.grid.isLandscape

@RequiresApi(34)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayAudioScreen(
        playAudioViewModel: PlayAudioViewModel,
        audioViewModel: AudioViewModel,
        audioFileIndex: Int,
        navController: NavHostController,
) {
    val audioList = audioViewModel.audioList.collectAsStateWithLifecycle().value
    val currentIndex = rememberSaveable { mutableIntStateOf(audioFileIndex) }
    val audioFile = audioList[audioFileIndex]
    val uri = audioFile.uri
    val context = LocalContext.current
    val state = playAudioViewModel.playbackState.collectAsStateWithLifecycle().value
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(Color.Transparent, darkIcons = false)
    MaterialTheme(darkColorScheme()) {
        XPlayerScreen(playAudioViewModel, audioViewModel, audioList, navController, currentIndex)
    }
    LaunchedEffect(state) {
        if (state == PlaybackState.IDLE) {
            Log.d("INSIDE_LAUNCHED_EFFECT_IDLE", "PlayAudioScreen: $state")
            playAudioViewModel.play(uri, context)
        } else if (state == PlaybackState.STOP) {
            Log.d("INSIDE_LAUNCHED_EFFECT_STOP", "PlayAudioScreen: $state")
            playAudioViewModel.playNext(audioList, currentIndex, context, false)
        }
    }

    BackHandler {
        playAudioViewModel.isLooping.value = false
        playAudioViewModel.isListLooping.value = false
        playAudioViewModel.pause()
        playAudioViewModel.playbackProgress.value = 0
        playAudioViewModel.setPlayBackState(PlaybackState.IDLE)
        navController.popBackStack()
    }
}

@Composable
fun XPlayerScreen(
        playAudioViewModel: PlayAudioViewModel,
        audioViewModel: AudioViewModel,
        audioList: MutableList<AudioItem>,
        navController: NavHostController,
        currentIndex: MutableIntState
) {

    val currentAudio = audioList[currentIndex.intValue]
    val albumArtUri =
            if (currentAudio.albumId != 0L) {
                ContentUris.withAppendedId(
                        android.net.Uri.parse("content://media/external/audio/albumart"),
                        currentAudio.albumId
                )
            } else null
    val progress = playAudioViewModel.playbackProgress.collectAsStateWithLifecycle()
    val dominantColor = Color.Magenta.copy(0.7f)
    val backGroundColor = listOf(Color.Black.copy(0.9f), dominantColor, Color.Black.copy(0.9f))
    Surface {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(backGroundColor))) {
            Column(
                    Modifier.padding(WindowInsets.systemBars.asPaddingValues()),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                XPlayerTopBar(
                        Modifier,
                        navController,
                        playAudioViewModel,
                        audioList[currentIndex.intValue],
                        audioViewModel
                )
                AudioThumbNail(albumArtUri)
                MarqueeText(text = audioList[currentIndex.intValue].displayName)
                SliderWithTimer(
                        progress.value,
                        playAudioViewModel.getDuration(),
                        timeDuration = playAudioViewModel.getTimeDuration(),
                        currentTime = playAudioViewModel.getCurrentTimeDuration()
                ) {
                    playAudioViewModel.run {
                        setPlayBackState(PlaybackState.SEEKING)
                        seekTo(it)
                        setStateAfterSeek()
                    }
                }
                PlayBackController(playAudioViewModel, audioViewModel, currentIndex)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarqueeText(text: String) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp)
                            .basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused)
                            .focusRequester(focusRequester)
                            .focusable()
                            .clickable(interactionSource = interactionSource, indication = null) {
                                focusRequester.requestFocus()
                            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Visible
    )
}

fun getDominantColor(): Color {
    return Color.Magenta.copy(0.7f)
}

@Composable
fun AudioThumbNail(
        albumArtUri: android.net.Uri?,
) {
    Box(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp)) {
        if (albumArtUri == null) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .fillMaxHeight(if (isLandscape()) 0.5f else 0.7f)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .background(Color.White.copy(0.4f))
            ) {
                Icon(
                        imageVector = Icons.Outlined.Audiotrack,
                        contentDescription = "Audio Track",
                        modifier = Modifier.size(200.dp).align(Alignment.Center),
                        tint = Color.Magenta.copy(0.7f)
                )
            }
        } else {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .fillMaxHeight(if (isLandscape()) 0.5f else 0.7f)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .background(Color.White.copy(0.1f)),
                    contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                        model = albumArtUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                        contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun PlayBackController(
        playAudioViewModel: PlayAudioViewModel,
        audioViewModel: AudioViewModel,
        currentIndex: MutableIntState,
) {
    val state by playAudioViewModel.playbackState.collectAsStateWithLifecycle()
    val isLooping by playAudioViewModel.isLooping.collectAsStateWithLifecycle()
    val isShuffling by playAudioViewModel.isShuffling.collectAsStateWithLifecycle()
    val isListLooping by playAudioViewModel.isListLooping.collectAsStateWithLifecycle()
    val audioList by audioViewModel.audioList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val playPauseIcon =
            if (state == PlaybackState.IDLE ||
                            state == PlaybackState.PAUSED ||
                            state == PlaybackState.STOP ||
                            state == PlaybackState.COMPLETED
            )
                    Icons.Filled.PlayCircle
            else Icons.Filled.PauseCircle
    val repeatIcon =
            if (isListLooping) Icons.Outlined.RepeatOn
            else if (isLooping) Icons.Outlined.RepeatOne else Icons.Outlined.Repeat
    val shuffleIcon = if (isShuffling) Icons.Outlined.ShuffleOn else Icons.Outlined.Shuffle
    Row(
            Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
                onClick = {
                    playAudioViewModel.isShuffling.value = !playAudioViewModel.isShuffling.value
                },
                modifier = Modifier.size(70.dp)
        ) {
            Icon(
                    imageVector = shuffleIcon,
                    contentDescription = "Shuffle Button",
                    modifier = Modifier.size(25.dp)
            )
        }

        IconButton(
                onClick = { playAudioViewModel.playPrevious(audioList, currentIndex, context) },
                modifier = Modifier.size(50.dp)
        ) {
            Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = "Skip Previous",
                    modifier = Modifier.size(45.dp)
            )
        }

        IconButton(
                onClick = {
                    if (state == PlaybackState.PLAYING) {
                        playAudioViewModel.pause()
                    } else {
                        playAudioViewModel.resume()
                    }
                },
                modifier = Modifier.size(90.dp)
        ) {
            Icon(
                    imageVector = playPauseIcon,
                    contentDescription = "Play Pause",
                    modifier = Modifier.size(60.dp)
            )
        }

        IconButton(
                onClick = { playAudioViewModel.playNext(audioList, currentIndex, context, true) },
                modifier = Modifier.size(50.dp)
        ) {
            Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = "Skip Next",
                    modifier = Modifier.size(40.dp)
            )
        }
        IconButton(
                onClick = { playAudioViewModel.repeatCurrent() },
                modifier = Modifier.size(70.dp)
        ) {
            Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Repeat Button",
                    modifier = Modifier.size(25.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderWithTimer(
        progress: Int,
        range: Int = 5,
        timeDuration: String,
        currentTime: String,
        onValueChange: (Int) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
    ) {
        Text(text = currentTime, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.width(4.dp))
        Slider(
                value = progress.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..if (range > 0) range.toFloat() else 2f,
                thumb = {
                    SliderDefaults.Thumb(
                            interactionSource,
                            colors = SliderDefaults.colors(thumbColor = Color.White),
                            thumbSize = DpSize(17.dp, 17.dp)
                    )
                },
                track = {
                    SliderDefaults.Track(
                            sliderState = it,
                            colors = SliderDefaults.colors(activeTrackColor = Color.White),
                            modifier = Modifier.fillMaxWidth((0.89).toFloat())
                    )
                },
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = timeDuration, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun XPlayerTopBar(
        modifier: Modifier = Modifier,
        navController: NavHostController,
        playAudioViewModel: PlayAudioViewModel,
        audioItem: AudioItem,
        audioViewModel: AudioViewModel
) {
    val showDropDownMenu = rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Row(modifier.fillMaxWidth().padding(2.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
                onClick = {
                    playAudioViewModel.isLooping.value = false
                    playAudioViewModel.isListLooping.value = false
                    playAudioViewModel.pause()
                    playAudioViewModel.playbackProgress.value = 0
                    playAudioViewModel.setPlayBackState(PlaybackState.IDLE)
                    navController.popBackStack()
                }
        ) {
            Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back Button"
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { playAudioViewModel.shareIntent(context) }) {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share Button")
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = { showDropDownMenu.value = true }) {
            XPlayerTopBarMenu(showDropDownMenu, audioItem, audioViewModel)
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "More Options")
        }
    }
}

@Composable
fun XPlayerTopBarMenu(
        showDropDownMenu: MutableState<Boolean> = mutableStateOf(true),
        audioItem: AudioItem,
        audioViewModel: AudioViewModel
) {
    val context = LocalContext.current
    var showFileInfo by remember { mutableStateOf(false) }
    val file = remember { mutableStateOf(audioItem) }
    val menuItems =
            listOf(
                    Pair("Open with") {
                        audioViewModel.openWith(audioItem, context)
                        showDropDownMenu.value = false
                    },
                    Pair("File info") {
                        showFileInfo = true
                        showDropDownMenu.value = false
                    },
                    Pair("Playback speed") {}
            )

    DropdownMenu(
            expanded = showDropDownMenu.value,
            onDismissRequest = { showDropDownMenu.value = false },
            offset = DpOffset(0.dp, (-30).dp)
    ) {
        menuItems.forEach { menuItem ->
            DropdownMenuItem(
                    text = { Text(text = menuItem.first) },
                    onClick = { menuItem.second.invoke() },
                    modifier = Modifier.padding(end = 50.dp, top = 5.dp)
            )
        }
    }

    AnimatedVisibility(
            showFileInfo,
            enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(900)),
            exit = slideOutHorizontally()
    ) {
        Dialog(
                onDismissRequest = { showFileInfo = false },
                properties =
                        DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                                SecureFlagPolicy.Inherit,
                                usePlatformDefaultWidth = false,
                                decorFitsSystemWindows = true
                        )
        ) { FileInfo(currentFile = file) { showFileInfo = false } }
    }
}
