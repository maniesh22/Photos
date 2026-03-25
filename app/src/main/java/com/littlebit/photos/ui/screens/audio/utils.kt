package com.littlebit.photos.ui.screens.audio

import android.content.ContentUris
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.Formatter.formatFileSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.littlebit.photos.model.AudioItem

@Composable
fun FileInfo(currentFile: MutableState<AudioItem>, removeFileInfo: () -> Unit = {}) {
    val fileSize = formatFileSize(LocalContext.current, currentFile.value.size)
    val albumArtUri =
            ContentUris.withAppendedId(
                    android.net.Uri.parse("content://media/external/audio/albumart"),
                    currentFile.value.albumId
            )
    Surface {
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
            if (currentFile.value.albumId != 0L) {
                AsyncImage(
                        model = albumArtUri,
                        contentDescription = "",
                        modifier =
                                Modifier.fillMaxWidth()
                                        .fillMaxHeight(0.3f)
                                        .align(Alignment.TopStart),
                        contentScale = ContentScale.FillBounds,
                )
            } else {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .fillMaxHeight(0.4f)
                                        .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Audiotrack,
                            contentDescription = "Audio Icon",
                            tint = Color.Magenta.copy(0.7f),
                            modifier = Modifier.size(64.dp)
                    )
                }
            }
            IconButton(
                    onClick = { removeFileInfo() },
                    modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back"
                )
            }
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .align(Alignment.TopStart)
                                    .padding(PaddingValues(12.dp)),
                    verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.3f).padding(22.dp))
                Text(
                        text = currentFile.value.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                        text = fileSize + ", ${currentFile.value.dateAdded}",
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                        text = currentFile.value.duration,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Outlined.Audiotrack,
                            contentDescription = "Audio Icon",
                            modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                            text = currentFile.value.path,
                            style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Calender Icon",
                            modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                            text = "Modified " + currentFile.value.dateAdded,
                            style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun AudioItem(
        audioFile: AudioItem,
        onClick: () -> Unit,
        audioViewModel: AudioViewModel,
        index: Int,
        showFileInfo: () -> Unit
) {
    val context = LocalContext.current
    val isSelectionInProcess by audioViewModel.isSelectionInProcess.collectAsStateWithLifecycle()
    var showMore by remember { mutableStateOf(false) }
    val backGround by
            animateColorAsState(
                    targetValue = getAudioItemColor(audioFile),
                    label = "",
                    animationSpec = spring()
            )

    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)

    Surface(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(
                                    PaddingValues(
                                            start = 12.dp,
                                            end = 12.dp,
                                            top = 4.dp,
                                            bottom = 4.dp
                                    )
                            ),
            shape = MaterialTheme.shapes.medium,
    ) {
        Row(
                Modifier.fillMaxWidth()
                        .background(backGround)
                        .padding(PaddingValues(11.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                    onLongPress = {
                                        if (!isSelectionInProcess) {
                                            audioViewModel.setSelectedAudio(index)
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
                                        if (isSelectionInProcess)
                                                audioViewModel.setSelectedAudio(index)
                                        else onClick()
                                    }
                            )
                        },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.7f))
            ) {
                val albumArtUri =
                        ContentUris.withAppendedId(
                                android.net.Uri.parse("content://media/external/audio/albumart"),
                                audioFile.albumId
                        )
                if (audioFile.albumId != 0L) {
                    Box {
                        AsyncImage(
                                model = albumArtUri,
                                contentDescription = "",
                                modifier = Modifier.size(44.dp).align(Alignment.Center),
                                contentScale = ContentScale.Crop
                        )
                        Icon(
                                imageVector = Icons.Outlined.Audiotrack,
                                contentDescription = "Audio Icon",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier =
                                        Modifier.padding(PaddingValues(4.dp))
                                                .size(12.dp)
                                                .align(Alignment.TopEnd)
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        Icon(
                                imageVector = Icons.Outlined.Audiotrack,
                                contentDescription = "Audio Icon",
                                tint = Color.Magenta.copy(0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                        text = audioFile.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(0.9f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                        text =
                                formatFileSize(LocalContext.current, audioFile.size) +
                                        ", ${audioFile.dateAdded}",
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                )
            }
            Spacer(modifier = Modifier.weight(0.2f))
            IconButton(
                    onClick = {
                        if (isSelectionInProcess) {
                            audioViewModel.setSelectedAudio(index)
                        } else showMore = !showMore
                    }
            ) {
                val icon = getIcon(isSelectionInProcess, audioFile)
                val tint = getTint(isSelectionInProcess, audioFile)
                Icon(imageVector = icon, contentDescription = "More Options", tint = tint)

                DropdownMenu(
                        expanded = showMore,
                        onDismissRequest = { showMore = false },
                        offset = DpOffset(0.dp, (-30).dp),
                        modifier = Modifier.fillMaxWidth(0.4f),
                ) {
                    DropdownMenuItem(
                            text = { Text(text = "Select") },
                            onClick = {
                                audioViewModel.setSelectedAudio(index)
                                showMore = false
                            }
                    )
                    DropdownMenuItem(
                            text = { Text(text = "Select All") },
                            onClick = {
                                audioViewModel.selectAllAudio()
                                showMore = false
                            }
                    )
                    DropdownMenuItem(
                            text = { Text(text = "Share") },
                            onClick = {
                                audioViewModel.shareAudio(audioFile, context)
                                showMore = false
                            }
                    )
                    DropdownMenuItem(
                            text = { Text(text = "Open with") },
                            onClick = {
                                audioViewModel.openWith(audioFile, context)
                                showMore = false
                            }
                    )
                    DropdownMenuItem(
                            text = { Text(text = "File info") },
                            onClick = {
                                showFileInfo()
                                showMore = false
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun getAudioItemColor(audioFile: AudioItem) =
        if (!audioFile.isSelected) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.primary.copy(0.3f)

@Composable
private fun getTint(isSelectionInProcess: Boolean, audioFile: AudioItem) =
        if (isSelectionInProcess && audioFile.isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface

@Composable
private fun getIcon(isSelectionInProcess: Boolean, audioFile: AudioItem) =
        if (isSelectionInProcess && audioFile.isSelected) Icons.Outlined.CheckCircle
        else if (isSelectionInProcess) Icons.Outlined.Circle else Icons.Outlined.MoreVert
