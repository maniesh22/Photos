package com.littlebit.photos.ui.screens.audio

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.model.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class AudioViewModel @Inject constructor(private val repository: MediaRepository) : ViewModel() {
    private val _audioItemList = MutableStateFlow(mutableListOf<AudioItem>())
    val audioList = _audioItemList
    private val selectedAudioList = MutableStateFlow(hashMapOf<Long, Int>())
    val selectedAudios = MutableStateFlow(0)
    val isSelectionInProcess = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)

    fun loadAudio(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            try {
                val result = repository.getAudioList(context)
                _audioItemList.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading.value = false
        }
    }

    fun refreshAudioList(context: Context) {
        loadAudio(context)
    }

    fun setSelectedAudio(audioIndex: Int) {
        _audioItemList.value[audioIndex].isSelected = !_audioItemList.value[audioIndex].isSelected
        isSelectionInProcess.value = _audioItemList.value.any { it.isSelected }
        if (_audioItemList.value[audioIndex].isSelected) {
            selectedAudioList.value[_audioItemList.value[audioIndex].id] = audioIndex
            selectedAudios.value++
        } else {
            if (selectedAudioList.value.containsKey(_audioItemList.value[audioIndex].id)) {
                selectedAudioList.value.remove(_audioItemList.value[audioIndex].id)
                selectedAudios.value--
            }
        }
        Log.d(
                "AUDIO_SIZE_LIST",
                "setSelectedAudio: ${selectedAudioList.value.size} ||  ${selectedAudios.value}"
        )
    }

    fun selectAllAudio() {
        viewModelScope.launch(Dispatchers.Default) {
            _audioItemList.value.forEachIndexed { index, audio ->
                audio.isSelected = true
                selectedAudioList.value[audio.id] = index
            }
            isSelectionInProcess.value = true
            selectedAudios.value = selectedAudioList.value.size
        }
    }

    fun unSelectAllAudio() {
        viewModelScope.launch(Dispatchers.Default) {
            selectedAudioList.value.forEach { index ->
                _audioItemList.value[index.value].isSelected = false
            }
            selectedAudioList.value.clear()
            isSelectionInProcess.value = false
            selectedAudios.value = 0
            Log.d(
                    "AUDIO_SIZE_LIST",
                    "unSelectAll: ${selectedAudioList.value.size} ||  ${selectedAudios.value}"
            )
        }
    }

    fun shareAudio(audioItemFile: AudioItem, context: Context) {
        val shareIntent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, audioItemFile.uri)
                    type = "audio/*"
                }
        Intent.createChooser(shareIntent, "Share Audio")
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                .also { intent -> context.startActivity(intent) }
    }

    fun openWith(audioItemFile: AudioItem, context: Context) {
        val openIntent =
                Intent().apply {
                    action = Intent.ACTION_VIEW
                    putExtra(Intent.EXTRA_STREAM, audioItemFile.uri)
                    type = "audio/*"
                }
        Intent.createChooser(openIntent, "Open With")
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                .also { intent -> context.startActivity(intent) }
    }

    fun getSelectedMemorySize(context: Context): String {
        var totalSize = 0L
        selectedAudioList.value.forEach { (_, pair) ->
            totalSize += _audioItemList.value[pair].size
        }
        return formatFileSize(context, totalSize)
    }

    fun shareSelectedAudios(): Intent {
        val shareIntent =
                Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(
                            Intent.EXTRA_STREAM,
                            ArrayList(
                                    selectedAudioList.value.map { (_, pair) ->
                                        _audioItemList.value[pair].uri
                                    }
                            )
                    )
                    type = "audio/*"
                }
        return Intent.createChooser(shareIntent, "Share Audios")
    }

    fun moveToTrashSelectedAudios(
            context: Context,
            trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    ) {
        val contentResolver = context.contentResolver
        viewModelScope.launch(Dispatchers.Default) {
            val selectedImages =
                    selectedAudioList.value.map { (_, pair) -> _audioItemList.value[pair].uri }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                selectedImages.forEach { contentResolver.delete(it, null, null) }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.R
            ) {
                selectedImages.forEach { contentResolver.delete(it, null, null) }
            } else {
                val intentSender =
                        MediaStore.createTrashRequest(contentResolver, selectedImages, true)
                                .intentSender
                trashLauncher.launch(intentSender.let { IntentSenderRequest.Builder(it).build() })
            }
        }
    }

    fun removeAudiosFromList(context: Context, message: String = "Moved to trash") {
        viewModelScope.launch(Dispatchers.Default) {
            val indicesToRemove = mutableListOf<Int>()
            selectedAudioList.value.forEach { item -> indicesToRemove.add(item.value) }
            audioList.value =
                    audioList
                            .value
                            .filterIndexed { index, _ -> index !in indicesToRemove }
                            .toMutableList()
            selectedAudioList.value.clear()
            selectedAudios.value = 0
            isSelectionInProcess.value = false
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
                            android.Manifest.permission.READ_MEDIA_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                loadAudio(applicationContext)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                loadAudio(applicationContext)
            }
        }
    }

    fun isSelectionProgress(): Boolean {
        return isSelectionInProcess.value
    }

    fun deleteSelected(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedAudios = getSelectedAudios()
                selectedAudios.forEach { audio ->
                    context.contentResolver.delete(audio.uri, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        removeAudiosFromList(context, "Deleted")
    }

    private fun getSelectedAudios(): List<AudioItem> {
        return selectedAudioList.value.map { (_, index) -> _audioItemList.value[index] }
    }
}
