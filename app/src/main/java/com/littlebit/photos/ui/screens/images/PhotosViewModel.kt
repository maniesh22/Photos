package com.littlebit.photos.ui.screens.images

import android.Manifest
import android.app.Activity
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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.model.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PhotosViewModel @Inject constructor(private val mediaRepository: MediaRepository) :
        ViewModel() {
    private val _photos = MutableStateFlow(mutableListOf<PhotoItem>())
    private val _photoGroups = MutableStateFlow(mutableListOf<ImageGroup>())
    val isLoading = MutableStateFlow(false)
    val photoGroups = _photoGroups
    val photos = _photos
    val selectedImages = MutableStateFlow(0)
    val selectedImageList = MutableStateFlow(hashMapOf<Long, Pair<Int, Int>>())

    private fun loadMedia(context: Context) {
        Log.d("PHOTOS_VIEW_MODEL", "addPhotosGroupedByDate: Inside LOAD_MEDIA")
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val media =
                        mediaRepository.getImagesGroupedByDate(context.contentResolver, isLoading)
                _photoGroups.value = media.first
                photos.value = media.second
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun addPhotosGroupedByDate(context: Context) {
        Log.d("PHOTOS_VIEW_MODEL", "addPhotosGroupedByDate: Inside ADD_PHOTOS")
        viewModelScope.launch(Dispatchers.IO) {
            mediaRepository.addImagesGroupedByDate(
                    context.contentResolver,
                    _photoGroups,
                    _photos,
                    isLoading
            )
        }
    }

    fun removeImageFromGrid(listGroupIndex: Int, imageIndex: MutableIntState) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (listGroupIndex < _photoGroups.value.size) {
                    if (imageIndex.intValue < _photoGroups.value[listGroupIndex].images.size) {
                        _photoGroups.value.removeAt(listGroupIndex)
                        if (_photoGroups.value[listGroupIndex].images.size == 0) {
                            _photoGroups.value.removeAt(listGroupIndex)
                        }
                    }
                    if (imageIndex.intValue < _photos.value.size) {
                        _photos.value.removeAt(imageIndex.intValue)
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    fun refresh(context: Context) = loadMedia(context)

    fun markImages(imageGroup: ImageGroup, listIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            var markedAll = true

            if (imageGroup.images.any { !it.isSelected }) {
                markedAll = false
            }

            imageGroup.images.forEachIndexed { index, photoItem ->
                if (markedAll) {
                    photoItem.isSelected = false
                    selectedImageList.value.remove(photoItem.id)
                } else {
                    if (!photoItem.isSelected) {
                        photoItem.isSelected = true
                        selectedImageList.value[photoItem.id] = Pair(listIndex, index)
                    }
                }
            }
            selectedImages.value = selectedImageList.value.size
        }
    }

    fun unSelectAllImages() {
        viewModelScope.launch(Dispatchers.Default) {
            selectedImageList.value.forEach {
                _photoGroups.value[it.value.first].images[it.value.second].isSelected = false
            }
            selectedImages.value = 0
            selectedImageList.value.clear()
        }
    }

    fun getSelectedMemorySize(context: Context): String {
        var totalSize = 0L
        selectedImageList.value.forEach {
            totalSize += _photoGroups.value[it.value.first].images[it.value.second].size
        }
        return formatFileSize(context, totalSize)
    }

    fun shareSelectedImages(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = "image/*"
        val selectedImages = getSelectedImages()
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedImages))
        return shareIntent
    }

    private fun getSelectedImages(): List<Uri?> {

        val list =
                selectedImageList.value.values.map { pair ->
                    val listIndex = pair.first
                    val imageIndex = pair.second
                    val uri = _photoGroups.value[listIndex].images[imageIndex].uri
                    uri
                }
        return list
    }

    fun moveToTrashSelectedImages(
            context: Context,
            trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    ) {
        val contentResolver = context.contentResolver
        viewModelScope.launch(Dispatchers.Default) {
            val selectedImages = getSelectedImages()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intentSender =
                        MediaStore.createTrashRequest(contentResolver, selectedImages, true)
                                .intentSender
                trashLauncher.launch(intentSender.let { IntentSenderRequest.Builder(it).build() })
            }
        }
    }

    fun shareImage(
            listIndex: Int,
            imageIndex: Int,
            shareLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                _photoGroups.value[listIndex].images[imageIndex].uri
        )
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareLauncher.launch(Intent.createChooser(shareIntent, "Share Photo"))
    }

    fun moveToTrash(
            context: Context,
            listIndex: Int,
            imageIndex: Int,
            trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
            showDeleteDialog: MutableState<Boolean>
    ) {
        try {
            val contentResolver = context.contentResolver
            val selectedImages = mutableListOf<Uri>()
            _photoGroups.value[listIndex].images[imageIndex].uri?.let { selectedImages.add(it) }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ||
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                    Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            ) {
                showDeleteDialog.value = true
            } else {
                val intentSender =
                        MediaStore.createTrashRequest(contentResolver, selectedImages, true)
                                .intentSender
                trashLauncher.launch(intentSender.let { IntentSenderRequest.Builder(it).build() })
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun removeImagesFromList(context: Context, message: String = "Moved to trash") {
        viewModelScope.launch(Dispatchers.Default) {
            val listMap = mutableMapOf<Int, MutableList<Int>>()
            selectedImageList.value.forEach { item ->
                val listIndex = item.value.first
                val imageIndex = item.value.second
                if (listMap.containsKey(listIndex)) {
                    listMap[listIndex]?.add(imageIndex)
                } else listMap[listIndex] = mutableListOf(imageIndex)
            }
            listMap.forEach { item ->
                val indicesToRemove = item.value
                val listIndex = item.key
                if (photoGroups.value.size > listIndex) {
                    val filteredList =
                            photoGroups.value[listIndex].images.filterIndexed { index, _ ->
                                index !in indicesToRemove
                            }
                    photoGroups.value[listIndex].images.clear()
                    photoGroups.value[listIndex].images.addAll(filteredList)
                }
            }
            photoGroups.value =
                    photoGroups
                            .value
                            .filterIndexed { _, item -> item.images.size != 0 }
                            .toMutableList()
            selectedImageList.value.clear()
            selectedImages.value = 0
            // FIX: Toast must be called on Main thread
            withContext(Dispatchers.Main) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deletePhoto(listIndex: Int, imageIndex: MutableIntState, context: Context) {
        val contentResolver = context.contentResolver
        val uri = _photoGroups.value[listIndex].images[imageIndex.intValue].uri

        if (uri != null) {
            if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    contentResolver?.delete(uri, null, null)
                    _photoGroups.value[listIndex].images.removeAt(imageIndex.intValue)
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    Log.d("PERMISSION", "deletePhoto: Deleted")
                    if (_photoGroups.value[listIndex].images.size == 0) {
                        _photoGroups.value.removeAt(listIndex)
                    } else {
                        imageIndex.intValue--
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    Log.d("PERMISSION", "deletePhoto: Not Deleted")
                }
            } else {
                ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1001
                )
                Log.d("PERMISSION", "deletePhoto: Permission not granted")
            }
        }
    }

    fun getData(applicationContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (_photoGroups.value.isEmpty()) addPhotosGroupedByDate(applicationContext)
                else loadMedia(applicationContext)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (_photoGroups.value.isEmpty()) addPhotosGroupedByDate(applicationContext)
                else loadMedia(applicationContext)
            }
        }
    }

    fun isSelectionInProgress(): Boolean {
        return selectedImages.value > 0
    }

    fun deleteSelected(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedImages = getSelectedImages()
                selectedImages.forEach { uri -> context.contentResolver.delete(uri!!, null, null) }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        removeImagesFromList(context, "Deleted")
    }
}
