package com.littlebit.photos.ui.screens.videos

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter.formatFileSize
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.model.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class VideoViewModel @Inject constructor(private val repository: MediaRepository) : ViewModel() {

    private val _videoList = MutableStateFlow(mutableListOf<VideoItem>())
    val videoList = _videoList
    private val _videoGroups = MutableStateFlow(mutableListOf<VideoGroup>())
    val videoGroups = _videoGroups
    val isLoading = MutableStateFlow(false)
    val selectedVideoList = MutableStateFlow(hashMapOf<Long, Pair<Int, Int>>())
    val selectedVideos = MutableStateFlow(0)

    private fun loadVideos(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.loadVideos(context, isLoading)
                _videoList.value = result.first
                _videoGroups.value = result.second
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addVideosGroupedByDate(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addVideosGroupedByDate(_videoGroups, _videoList, isLoading, context)
        }
    }

    fun refreshVideos(context: Context) = loadVideos(context)

    fun selectVideo(id: Long, listIndex: Int, videoIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            if (selectedVideoList.value.containsKey(id)) {
                selectedVideoList.value.remove(id)
                _videoGroups.value[listIndex].videos[videoIndex].isSelected = false
                selectedVideos.value--
            } else {
                selectedVideoList.value[id] = Pair(listIndex, videoIndex)
                _videoGroups.value[listIndex].videos[videoIndex].isSelected = true
                selectedVideos.value++
            }
            Log.d("SELECTION", "selectVideo: ${selectedVideoList.value.size}")
        }
    }

    fun selectAllVideos(group: VideoGroup, listIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            var markedAll = true
            if (group.videos.any { !it.isSelected }) {
                markedAll = false
            }
            group.videos.forEachIndexed { index, videoItem ->
                if (markedAll) {
                    videoItem.isSelected = false
                    selectedVideoList.value.remove(videoItem.id)
                    selectedVideos.value--
                } else {
                    if (!videoItem.isSelected) {
                        videoItem.isSelected = true
                        selectedVideoList.value[videoItem.id] = Pair(listIndex, index)
                        selectedVideos.value++
                    }
                }
            }
        }
    }

    fun unSelectAllVideos() {
        viewModelScope.launch(Dispatchers.Default) {
            selectedVideoList.value.forEach { (_, pair) ->
                _videoGroups.value[pair.first].videos[pair.second].isSelected = false
            }
            selectedVideoList.value.clear()
            selectedVideos.value = 0
        }
    }

    fun getSelectedMemorySize(context: Context): String {
        var totalSize = 0L
        selectedVideoList.value.forEach { (_, pair) ->
            totalSize += _videoGroups.value[pair.first].videos[pair.second].size
        }
        return formatFileSize(context, totalSize)
    }

    fun shareSelectedVideos(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = "video/*"
        val selectedUris = ArrayList<Uri>()
        selectedVideoList.value.forEach { (_, pair) ->
            _videoGroups.value[pair.first].videos[pair.second].uri?.let { selectedUris.add(it) }
        }
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, selectedUris)
        return shareIntent
    }

    fun moveToTrashSelectedVideos(
            context: Context,
            trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    ) {
        val contentResolver = context.contentResolver
        viewModelScope.launch(Dispatchers.Default) {
            val selectedUris = mutableListOf<Uri>()
            selectedVideoList.value.forEach { (_, pair) ->
                _videoGroups.value[pair.first].videos[pair.second].uri?.let { selectedUris.add(it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intentSender =
                        MediaStore.createTrashRequest(contentResolver, selectedUris, true)
                                .intentSender
                trashLauncher.launch(intentSender.let { IntentSenderRequest.Builder(it).build() })
            }
        }
    }

    fun removeVideosFromList(context: Context, message: String = "Moved to trash") {
        viewModelScope.launch(Dispatchers.Default) {
            val listMap = mutableMapOf<Int, MutableList<Int>>()
            selectedVideoList.value.forEach { item ->
                val listIndex = item.value.first
                val imageIndex = item.value.second
                if (listMap.containsKey(listIndex)) {
                    listMap[listIndex]?.add(imageIndex)
                } else listMap[listIndex] = mutableListOf(imageIndex)
            }
            listMap.forEach { item ->
                val indicesToRemove = item.value
                val listIndex = item.key
                if (_videoGroups.value.size > listIndex) {
                    val filteredList =
                            _videoGroups.value[listIndex].videos.filterIndexed { index, _ ->
                                index !in indicesToRemove
                            }
                    _videoGroups.value[listIndex].videos.clear()
                    _videoGroups.value[listIndex].videos.addAll(filteredList)
                }
            }
            _videoGroups.value =
                    _videoGroups
                            .value
                            .filterIndexed { _, item -> item.videos.size != 0 }
                            .toMutableList()
            selectedVideoList.value.clear()
            selectedVideos.value = 0
            // FIX: Toast must be called on Main thread
            withContext(Dispatchers.Main) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getData(applicationContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            android.Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (_videoGroups.value.isEmpty()) addVideosGroupedByDate(applicationContext)
                else loadVideos(applicationContext)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (_videoGroups.value.isEmpty()) addVideosGroupedByDate(applicationContext)
                else loadVideos(applicationContext)
            }
        }
    }

    fun isSelectionInProgress(): Boolean {
        return selectedVideos.value > 0
    }

    fun deleteSelected(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedUris = mutableListOf<Uri>()
                selectedVideoList.value.forEach { (_, pair) ->
                    _videoGroups.value[pair.first].videos[pair.second].uri?.let {
                        selectedUris.add(it)
                    }
                }
                selectedUris.forEach { uri -> context.contentResolver.delete(uri, null, null) }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        removeVideosFromList(context, "Deleted")
    }
}
