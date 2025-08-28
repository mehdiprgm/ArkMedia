package org.zendev.arkmedia.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import org.zendev.arkmedia.data.media.Video
import org.zendev.arkmedia.tools.convertNumberToDate

class VideoRepository(private val context: Context) {
    fun getAll(): MutableList<Video> {
        val list = mutableListOf<Video>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.RELATIVE_PATH
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val widthIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val size = it.getLong(sizeIndex)
                val date = it.getLong(dateIndex)
                val duration = it.getLong(durationIndex)
                val width = it.getInt(widthIndex)
                val height = it.getInt(heightIndex)
                val path = it.getString(pathIndex)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                list.add(
                    Video(
                        uri = contentUri,
                        name = name,
                        path = path,
                        createDate = convertNumberToDate(date),
                        size = size,
                        width = width,
                        height = height,
                        duration = duration
                    )
                )
            }

            list.sortedBy { it -> it.name.lowercase() }
        }

        return list
    }
}