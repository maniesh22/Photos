package com.littlebit.photos.ui.screens.search

import androidx.lifecycle.ViewModel
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.SearchItem
import com.littlebit.photos.model.VideoGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    /**
     * Searches across images, videos, and audio by display name. Accepts data lists directly
     * instead of ViewModel references to reduce coupling.
     */
    fun getSearchItems(
            imageGroups: List<ImageGroup>,
            videoGroups: List<VideoGroup>,
            audioList: List<AudioItem>,
            query: String
    ): List<SearchItem> {
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase()
        val result = mutableListOf<SearchItem>()

        imageGroups.forEachIndexed { listIndex, group ->
            group.images.forEachIndexed { index, photoItem ->
                if (photoItem.displayName.lowercase().contains(lowerQuery)) {
                    result.add(
                            SearchItem(
                                    title = photoItem.displayName,
                                    type = "image",
                                    url = photoItem.uri,
                                    listIndex = listIndex,
                                    index = index
                            )
                    )
                }
            }
        }

        videoGroups.forEachIndexed { listIndex, group ->
            group.videos.forEachIndexed { index, videoItem ->
                if (videoItem.displayName.lowercase().contains(lowerQuery)) {
                    result.add(
                            SearchItem(
                                    title = videoItem.displayName,
                                    type = "video",
                                    url = videoItem.uri,
                                    listIndex = listIndex,
                                    index = index
                            )
                    )
                }
            }
        }

        audioList.forEachIndexed { index, audioItem ->
            if (audioItem.displayName.lowercase().contains(lowerQuery)) {
                result.add(
                        SearchItem(
                                title = audioItem.displayName,
                                type = "audio",
                                url = audioItem.uri,
                                index = index
                        )
                )
            }
        }

        return result
    }
}
