package com.littlebit.photos.ui.screens.home


import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.outlined.ArrowDropDownCircle
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoCameraBack
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.screens.videos.grid.isLandscape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageItem(
    imageUri: Uri,
    onImageClick: (Uri) -> Unit = {},
    contentScale: ContentScale = ContentScale.Crop,
    image: PhotoItem,
    photosViewModel: PhotosViewModel,
    listIndex: Int,
    index: Int,

    ) {
    val selectedImages by photosViewModel.selectedImages.collectAsStateWithLifecycle()
    val selectedImageList by photosViewModel.selectedImageList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imageSize = if(isLandscape()) (LocalConfiguration.current.screenWidthDp-1)/5 else (LocalConfiguration.current.screenWidthDp-1)/3
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
    val size by animateDpAsState(
        targetValue = if (image.isSelected) (imageSize-10).dp else imageSize.dp,
        label = "",
        animationSpec = tween(90, 10)
    )
    val cornerSize by animateDpAsState(
        targetValue = if (image.isSelected) 20.dp else 0.dp,
        label = "",
        animationSpec = tween(90, 10)
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(141.dp)
            .background(if(image.isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
    ) {
        GlideImage(
            model = imageUri,
            contentDescription = null,
            modifier = Modifier
                .padding(PaddingValues(1.dp))
                .size(size)
                .clip(RoundedCornerShape(cornerSize))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            if (selectedImages == 0) {
                                photosViewModel.selectedImages.value++
                                image.isSelected = true
                                selectedImageList[image.id] = Pair(listIndex, index)
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
                            if (selectedImages > 0) {
                                image.isSelected = !image.isSelected
                                if (image.isSelected) {
                                    photosViewModel.selectedImages.value++
                                    selectedImageList[image.id] = Pair(listIndex, index)
                                } else {
                                    photosViewModel.selectedImages.value--
                                    selectedImageList.remove(image.id)
                                }
                            } else onImageClick(imageUri)
                        }
                    )
                },
            contentScale = contentScale,

        )
        val icon = if (image.isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
        val tint = if (selectedImages > 0) {
            if (image.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        } else Color.Transparent

        Icon(
            imageVector = icon, contentDescription = "Circle", tint = tint, modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageGroupSection(
    imageGroup: ImageGroup,
    imageIndex: MutableState<Int>,
    listIndex: Int,
    photosViewModel: PhotosViewModel,
    onImageClick: (index: Int) -> Unit
) {
    Column(
        Modifier.fillMaxWidth(1f)
    ) {
        DateWithMarkButton(date = imageGroup.date) {
            photosViewModel.markImages(imageGroup, listIndex)
        }
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            imageGroup.images.forEachIndexed { index, image ->
                ImageItem(
                    imageUri = image.uri!!,
                    {
                        imageIndex.value = index
                        onImageClick(index)
                    },
                    ContentScale.Crop,
                    image,
                    photosViewModel,
                    listIndex = listIndex,
                    index = index
                )
            }
        }
    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageGridList(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    scrollState: LazyListState,
    pullRefreshState: PullRefreshState
) {
    val imageGroups by photosViewModel.photoGroups.collectAsStateWithLifecycle()
    val isLoading by photosViewModel.isLoading.collectAsStateWithLifecycle()
    val imageIndex = remember {
        mutableIntStateOf(0)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
        state = scrollState,
    ) {
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
        items(imageGroups.size) { index ->
            val imageGroup = imageGroups[index]
            ImageGroupSection(
                imageGroup,
                imageIndex,
                listIndex = index,
                photosViewModel
            ) { clickedImageIndex ->
                navHostController.navigate(Screens.ImageDetailsScreen.route + "/${clickedImageIndex}/${index}") {
                    launchSingleTop = true
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (imageGroups.isEmpty()) {
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
                        text = "No Photos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "You can add photos to your phone and they will show up here",
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
    }
}


@Composable
fun DateWithMarkButton(
    modifier: Modifier = Modifier,
    date: String,
    onButtonClick: () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(PaddingValues(4.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //format day, date month
        Text(
            text = date,
            style = MaterialTheme.typography.labelLarge
        )
        IconButton(onClick = { onButtonClick() }) {
            Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Check Circle")
        }
    }
}


const val profile_image =
    "https://w0.peakpx.com/wallpaper/343/852/HD-wallpaper-iphone-planet-amoled-apple-galaxy-gold-life-space-strange-ultra.jpg"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    onClick: () -> Unit
) {
    val isImageLoading = remember {
        mutableStateOf(false)
    }
    Box {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            ),
            title = {
                Box(Modifier.fillMaxWidth()) {

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    /** To be added in future
                    AsyncImage(
                        model = profile_image,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .align(Alignment.BottomEnd)
                            .clickable {
                                onClick()
                            },
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            // Display a circular, indeterminate progress indicator
                            isImageLoading.value = true
                        },
                        onError = {
                            isImageLoading.value = false
                        },
                        onSuccess = {
                            isImageLoading.value = false
                        }
                    )
                    AnimatedVisibility(
                        visible = isImageLoading.value,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        CircularProgressIndicator(
                            Modifier
                                .padding(4.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    **/
                }
            },
            scrollBehavior = scrollBehavior,
            modifier = modifier
        )
        if (scrollBehavior.state.heightOffset == 0f)
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                thickness = (0.2).dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
    }
}

@Composable
fun HomeScreenBottomBar(
    modifier: Modifier = Modifier,
    currentScreen: MutableState<String>,
    imageScreenListState: LazyListState,
    videoScreenListState: LazyListState,
    audioScreenListState: LazyListState,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val photosSelected = currentScreen.value == Screens.HomeScreen.route
    val searchSelected = currentScreen.value == Screens.SearchScreen.route
    val audioSelected = currentScreen.value == Screens.AudioScreen.route
    val videoSelected = currentScreen.value == Screens.VideoGridScreen.route
    val photoIcon = if (photosSelected) Icons.Filled.Image else Icons.Outlined.Image
    val searchIcon = if (searchSelected) Icons.Filled.Search else Icons.Outlined.Search
    val audioIcon = if (audioSelected) Icons.Filled.Audiotrack else Icons.Outlined.Audiotrack
    val videoIcon =
        if (videoSelected) Icons.Filled.VideoCameraBack else Icons.Outlined.VideoCameraBack
    val onPhotoIconClick =
        getOnPhotoIconClick(imageScreenListState, photosSelected, coroutineScope, currentScreen)
    val onSearchIconClick = getOnSearchIconClick(keyboardController, currentScreen)
    val onAudioIconClick =
        getOnAudioIconClick(audioScreenListState, audioSelected, coroutineScope, currentScreen)
    val onVideoClickIcon =
        getOnVideoClickIcon(videoScreenListState, videoSelected, coroutineScope, currentScreen)
    BottomAppBar(
        modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(0.9f),
                shape = RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
            )
            .background(
                Brush.verticalGradient(getBottomBarContainerColor()),
                shape = RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
            ),
        containerColor = Color.Transparent,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = photosSelected,
            onClick = { onPhotoIconClick() },
            icon = { Icon(imageVector = photoIcon, contentDescription = "Image Icon") })

        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = searchSelected,
            onClick = { onSearchIconClick() },
            icon = { Icon(imageVector = searchIcon, contentDescription = "Search Icon") })

        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = audioSelected,
            onClick = { onAudioIconClick() },
            icon = { Icon(imageVector = audioIcon, contentDescription = "Audio Track Icon") })

        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = videoSelected,
            onClick = { onVideoClickIcon() },
            icon = { Icon(imageVector = videoIcon, contentDescription = "Video Camera Icon") })
        Spacer(modifier = Modifier.weight(1f))
    }

}

@Composable
private fun getBottomBarContainerColor() = listOf(
    BottomAppBarDefaults.containerColor.copy(0.3f),
    BottomAppBarDefaults.containerColor.copy(0.5f),
    BottomAppBarDefaults.containerColor.copy(0.7f),
    BottomAppBarDefaults.containerColor.copy(0.9f),
    BottomAppBarDefaults.containerColor
)

@Composable
private fun getOnVideoClickIcon(
    videoScreenListState: LazyListState,
    videoSelected: Boolean,
    coroutineScope: CoroutineScope,
    currentScreen: MutableState<String>
): () -> Unit = {
    if (videoScreenListState.firstVisibleItemIndex > 2 && videoSelected)
        coroutineScope.launch {
            videoScreenListState.animateScrollToItem(0)
        }
    else currentScreen.value = Screens.VideoGridScreen.route
}

@Composable
private fun getOnAudioIconClick(
    audioScreenListState: LazyListState,
    audioSelected: Boolean,
    coroutineScope: CoroutineScope,
    currentScreen: MutableState<String>
): () -> Unit = {
    if (audioScreenListState.firstVisibleItemIndex > 2 && audioSelected)
        coroutineScope.launch {
            audioScreenListState.animateScrollToItem(0)
        }
    else currentScreen.value = Screens.AudioScreen.route
}

@Composable
private fun getOnSearchIconClick(
    keyboardController: SoftwareKeyboardController?,
    currentScreen: MutableState<String>
): () -> Unit = {
    keyboardController?.show()
    currentScreen.value = Screens.SearchScreen.route
}

@Composable
private fun getOnPhotoIconClick(
    imageScreenListState: LazyListState,
    photosSelected: Boolean,
    coroutineScope: CoroutineScope,
    currentScreen: MutableState<String>
): () -> Unit = {
    if (imageScreenListState.firstVisibleItemIndex > 2 && photosSelected) {
        coroutineScope.launch {
            imageScreenListState.animateScrollToItem(0)
        }
    } else currentScreen.value = Screens.HomeScreen.route
}

@Composable
fun FloatingProfileDialog(
    showAlertDialog: MutableState<Boolean>,
    navHostController: NavHostController
) {
    if (showAlertDialog.value) {
        var yOffset by remember { mutableFloatStateOf(0f) }
        val animateDp = animateDpAsState(targetValue = yOffset.dp, label = "yOffset Animation")

        Dialog(
            onDismissRequest = {
                showAlertDialog.value = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(start = 10.dp, end = 10.dp))
                    .offset(y = animateDp.value)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, _, _ ->
                            yOffset += pan.y
                            if (yOffset > 100 || yOffset <= -100) {
                                showAlertDialog.value = false
                            }
                        }
                    },
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(6.dp))
                    ) {
                        IconButton(
                            onClick = {
                                showAlertDialog.value = false
                            },
                            Modifier.align(
                                Alignment.TopStart
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close Icon",
                            )
                        }
                        Text(
                            text = "010-Media-010",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(
                                Alignment.Center
                            )
                        )
                    }

                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(10.dp)),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .clickable { }
                                .padding(PaddingValues(10.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = profile_image,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .clickable {

                                    },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "User Name",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Email@xyz.com",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDownCircle,
                                contentDescription = "",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .padding(PaddingValues(10.dp))
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            MaterialTheme.shapes.large
                                        )
                                        .clip(shape = MaterialTheme.shapes.large)
                                        .clickable {

                                        }
                                ) {
                                    Text(
                                        text = "Manage your Account",
                                        modifier = Modifier.padding(
                                            PaddingValues(8.dp)
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(12.dp))
                    ) {
                        DialogItem(icon = Icons.Outlined.Settings, title = "Photos settings") {
                            showAlertDialog.value = false
                            navHostController.navigate(Screens.SettingsScreen.route) {
                                launchSingleTop = true
                            }
                        }
                        DialogItem(
                            icon = Icons.Outlined.DataExploration,
                            title = "Your data in Photos"
                        ) {

                        }
                        DialogItem(icon = Icons.AutoMirrored.Outlined.Help, title = "help") {

                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DialogItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .padding(PaddingValues(8.dp))
            .clickable(interactionSource, indication = null) {
                onClick()
            }
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}







fun getTotalMemorySize(
    photosViewModel: PhotosViewModel,
    audioViewModel: AudioViewModel,
    videoViewModel: VideoViewModel,
    context: Context
): String {
    val totalSelectedImages = photosViewModel.selectedImages.value
    val totalSelectedAudios = audioViewModel.selectedAudios.value
    return if (totalSelectedImages > 0) {
        photosViewModel.getSelectedMemorySize(context)
    } else if (totalSelectedAudios > 0) {
        audioViewModel.getSelectedMemorySize(context)
    } else {
        videoViewModel.getSelectedMemorySize(context)
    }
}


fun getTotalSelected(
    audioSelectionInProgress: Boolean,
    totalSelectedAudios: Int,
    photosSelectionInProgress: Boolean,
    totalSelectedImages: Int,
    totalSelectedVideos: Int
) =
    if (audioSelectionInProgress) totalSelectedAudios else if (photosSelectionInProgress) totalSelectedImages else totalSelectedVideos


fun unSelectAllSelected(
    audioSelectionInProgress: Boolean,
    audioViewModel: AudioViewModel,
    videoSelectionInProgress: Boolean,
    videoViewModel: VideoViewModel,
    photosViewModel: PhotosViewModel
): () -> Unit = {
    if (audioSelectionInProgress) {
        audioViewModel.unSelectAllAudio()
    } else if (videoSelectionInProgress) {
        videoViewModel.unSelectAllVideos()
    } else {
        photosViewModel.unSelectAllImages()
    }
}




