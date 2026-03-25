package com.littlebit.photos.model.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow

class MediaRepository {

    // Cached formatter — avoids allocating a new SimpleDateFormat on every call
    private val dateFormatter = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

    private fun formatDate(dateAdded: Long): String {
        return dateFormatter.format(Date(dateAdded * 1000))
    }

    // ──────────────────────── Images ────────────────────────

    fun getImagesGroupedByDate(
            contentResolver: ContentResolver,
            isLoading: MutableStateFlow<Boolean>
    ): Pair<MutableList<ImageGroup>, MutableList<PhotoItem>> {
        isLoading.value = true
        val photosList = mutableListOf<PhotoItem>()
        val projection =
                arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_MODIFIED,
                        MediaStore.Images.Media.SIZE
                )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val photoItem = PhotoItem(id, displayName, dateAdded, contentUri, size)
                photosList.add(photoItem)
            }
        }

        // FIX: Use dateAdded already read from cursor — eliminates the N+1 query
        val groupedImages = photosList.groupBy { formatDate(it.dateAdded) }
        val imagesGroupedByDate =
                groupedImages
                        .map { (date, imagesForDate) ->
                            ImageGroup(date, imagesForDate.toMutableList())
                        }
                        .toMutableList()

        isLoading.value = false
        return Pair(imagesGroupedByDate, photosList)
    }

    fun addImagesGroupedByDate(
            contentResolver: ContentResolver,
            photoGroups: MutableStateFlow<MutableList<ImageGroup>>,
            photos: MutableStateFlow<MutableList<PhotoItem>>,
            isLoading: MutableStateFlow<Boolean>
    ) {
        isLoading.value = true
        val projection =
                arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_MODIFIED,
                        MediaStore.Images.Media.SIZE
                )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val photoItem = PhotoItem(id, displayName, dateAdded, contentUri, size)

                photos.value.add(photoItem)

                val formattedDate = formatDate(dateAdded)

                if (photoGroups.value.isNotEmpty() && photoGroups.value.last().date != formattedDate
                ) {
                    photoGroups.value.add(ImageGroup(formattedDate, mutableListOf(photoItem)))
                } else if (photoGroups.value.isNotEmpty()) {
                    photoGroups.value.last().images.add(photoItem)
                } else {
                    photoGroups.value.add(ImageGroup(formattedDate, mutableListOf(photoItem)))
                }

                if (photoGroups.value.size > 1) {
                    isLoading.value = false
                }
            }
        }
        isLoading.value = false
    }

    // ──────────────────────── Audio ────────────────────────

    fun getAudioList(context: Context): MutableList<AudioItem> {
        val list = mutableListOf<AudioItem>()
        val contentResolver = context.contentResolver
        val projection =
                arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DATE_ADDED,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.IS_MUSIC,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID
                )
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val isMusicColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getString(durationColumn)
                val isMusic = cursor.getInt(isMusicColumn)
                val path = cursor.getString(pathColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val uri = Uri.withAppendedPath(queryUri, id.toString())

                // FIX: No longer creating MediaMetadataRetriever per item.
                // Thumbnails are loaded lazily in the UI via album art URI.
                val audio =
                        AudioItem(
                                id,
                                displayName,
                                path,
                                formatDuration(duration.toLong()),
                                size,
                                isMusic == 1,
                                uri,
                                albumId,
                                formatDate(dateAdded)
                        )
                list.add(audio)
            }
        }
        return list
    }

    private fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        if (hours > 0) return String.format("%02d h : %02d m : %02d s", hours, minutes, seconds)
        return String.format("%02d m : %02d s", minutes, seconds)
    }

    // ──────────────────────── Videos ────────────────────────

    fun loadVideos(
            context: Context,
            isLoading: MutableStateFlow<Boolean>
    ): Pair<MutableList<VideoItem>, MutableList<VideoGroup>> {
        isLoading.value = true
        val videos = mutableListOf<VideoItem>()
        val projection =
                arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATE_ADDED,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DURATION
                )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        val contentResolver = context.contentResolver
        val query =
                contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder
                )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val contentUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)

                // FIX: No longer loading Bitmap thumbnails eagerly.
                // Glide/Coil handles thumbnails from the content URI in the UI.
                videos.add(
                        VideoItem(id, displayName, dateAdded, contentUri, size, duration = duration)
                )
            }
        }

        val groupedVideos = videos.groupBy { formatDate(it.dateAdded) }
        val videoGroup =
                groupedVideos
                        .map { (date, vids) -> VideoGroup(date, vids.toMutableList()) }
                        .toMutableList()

        isLoading.value = false
        return Pair(videos, videoGroup)
    }

    fun addVideosGroupedByDate(
            videoGroups: MutableStateFlow<MutableList<VideoGroup>>,
            videos: MutableStateFlow<MutableList<VideoItem>>,
            isLoading: MutableStateFlow<Boolean>,
            context: Context
    ) {
        val contentResolver = context.contentResolver
        isLoading.value = true
        val projection =
                arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATE_ADDED,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DURATION
                )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getLong(durationColumn)

                // FIX: No eager thumbnail loading
                val videoItem =
                        VideoItem(id, displayName, dateAdded, contentUri, size, duration = duration)

                videos.value.add(videoItem)

                val formattedDate = formatDate(dateAdded)

                if (videoGroups.value.isNotEmpty() && videoGroups.value.last().date != formattedDate
                ) {
                    videoGroups.value.add(VideoGroup(formattedDate, mutableListOf(videoItem)))
                } else if (videoGroups.value.isNotEmpty()) {
                    videoGroups.value.last().videos.add(videoItem)
                } else {
                    videoGroups.value.add(VideoGroup(formattedDate, mutableListOf(videoItem)))
                    isLoading.value = false
                }
            }
        }
        isLoading.value = false
    }
}
