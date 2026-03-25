package com.littlebit.photos.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.littlebit.photos.model.SearchItem
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel

@Composable
fun SearchScreen(
        navHostController: NavHostController,
        searchViewModel: SearchViewModel,
        photosViewModel: PhotosViewModel,
        videoViewModel: VideoViewModel,
        audioViewModel: AudioViewModel,
        currentScreen: MutableState<String>
) {
    val searchItems = rememberSaveable { mutableStateOf(listOf<SearchItem>()) }
    val isInputFieldEmpty = remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Collect data flows once
    val imageGroups = photosViewModel.photoGroups.collectAsStateWithLifecycle().value
    val videoGroups = videoViewModel.videoGroups.collectAsStateWithLifecycle().value
    val audioList = audioViewModel.audioList.collectAsStateWithLifecycle().value

    Surface {
        Scaffold(
                topBar = {
                    SearchTopBar(
                            searchViewModel,
                            imageGroups,
                            videoGroups,
                            audioList,
                            searchItems,
                            currentScreen,
                            isInputFieldEmpty,
                            keyboardController,
                            focusRequester
                    )
                }
        ) {
            Box(modifier = Modifier.padding(it)) {
                SearchContent(
                        searchItems,
                        navHostController,
                        isInputFieldEmpty,
                        keyboardController,
                        focusRequester
                )
            }
        }
    }

    BackHandler {
        if (!focusManager.moveFocus(FocusDirection.Previous)) {
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
            currentScreen.value = Screens.HomeScreen.route
        }
    }
}

@Composable
fun SearchTopBar(
        searchViewModel: SearchViewModel,
        imageGroups: List<com.littlebit.photos.model.ImageGroup>,
        videoGroups: List<com.littlebit.photos.model.VideoGroup>,
        audioList: List<com.littlebit.photos.model.AudioItem>,
        searchItems: MutableState<List<SearchItem>>,
        currentScreen: MutableState<String>,
        isInputFieldEmpty: MutableState<Boolean>,
        keyboardController: SoftwareKeyboardController?,
        focusRequester: FocusRequester
) {

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(key1 = currentScreen.value == Screens.SearchScreen.route) {
        focusRequester.requestFocus()
    }
    val inputText = rememberSaveable { mutableStateOf("") }

    Row(
            Modifier.fillMaxWidth().padding(WindowInsets.systemBars.asPaddingValues()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
    ) {
        OutlinedTextField(
                value = inputText.value,
                onValueChange = {
                    inputText.value = it
                    if (it.isNotEmpty()) {
                        searchItems.value =
                                searchViewModel.getSearchItems(
                                        imageGroups,
                                        videoGroups,
                                        audioList,
                                        inputText.value
                                )
                    } else {
                        searchItems.value = listOf()
                    }
                    isInputFieldEmpty.value = it.isEmpty()
                },
                modifier = Modifier.focusRequester(focusRequester).fillMaxWidth().padding(16.dp),
                label = {
                    Text(
                            "Search",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions =
                        KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    focusRequester.freeFocus()
                                    searchItems.value =
                                            searchViewModel.getSearchItems(
                                                    imageGroups,
                                                    videoGroups,
                                                    audioList,
                                                    inputText.value
                                            )
                                }
                        ),
                leadingIcon = {
                    IconButton(
                            onClick = {
                                keyboardController?.hide()
                                currentScreen.value = Screens.HomeScreen.route
                            },
                    ) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Button"
                        )
                    }
                },
                trailingIcon = {
                    if (inputText.value.isNotEmpty()) {
                        IconButton(
                                onClick = {
                                    inputText.value = ""
                                    searchItems.value = listOf()
                                    isInputFieldEmpty.value = true
                                },
                        ) {
                            Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close Button"
                            )
                        }
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                interactionSource = interactionSource,
        )
    }
}

@Composable
fun SearchContent(
        searchItems: MutableState<List<SearchItem>>,
        navHostController: NavHostController,
        isInputFieldEmpty: MutableState<Boolean>,
        keyboardController: SoftwareKeyboardController?,
        focusRequester: FocusRequester
) {
    if (searchItems.value.isEmpty() && !isInputFieldEmpty.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Not Found", style = MaterialTheme.typography.titleLarge)
        }
    }

    LazyColumn {
        items(searchItems.value) { searchItem ->
            SearchItemRow(searchItem) {
                keyboardController?.hide()
                focusRequester.freeFocus()
                when (searchItem.type) {
                    "image" -> {
                        navHostController.navigate(
                                Screens.ImageDetailsScreen.route +
                                        "/${searchItem.index}/${searchItem.listIndex}"
                        ) { launchSingleTop = true }
                    }
                    "video" -> {
                        navHostController.navigate(
                                Screens.VideoScreen.route +
                                        "/${searchItem.index}/${searchItem.listIndex}"
                        ) { launchSingleTop = true }
                    }
                    else -> {
                        navHostController.navigate(
                                Screens.PlayAudioScreen.route + "/${searchItem.index}"
                        ) { launchSingleTop = true }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItemRow(searchItem: SearchItem, onClick: () -> Unit = {}) {
    Row(
            Modifier.fillMaxWidth().clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                searchItem.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp).fillMaxWidth(0.7f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 3
        )
        when (searchItem.type) {
            "image" -> {
                AsyncImage(
                        model = searchItem.url,
                        contentDescription = searchItem.type,
                        modifier = Modifier.padding(16.dp).size(98.dp)
                )
            }
            "video" -> {
                // Use video content URI directly — Glide/Coil can decode video thumbnails from URI
                AsyncImage(
                        model = searchItem.url,
                        contentDescription = searchItem.type,
                        modifier = Modifier.size(98.dp)
                )
            }
            else -> {
                // Audio — no embedded bitmap, show icon placeholder
                Icon(
                        imageVector = Icons.Outlined.Audiotrack,
                        contentDescription = "Audio Track",
                        tint = Color.Magenta
                )
            }
        }
    }
}
