package org.zendev.arkmedia.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import org.zendev.arkmedia.data.media.Audio
import org.zendev.arkmedia.tools.convertNumberToDate

class AudioRepository(private val context: Context) {

    fun getAll(): MutableList<Audio> {
        val list = mutableListOf<Audio>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.SIZE
        )

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val date = it.getLong(dateIndex)
                val path = it.getString(pathIndex)
                val duration = it.getLong(durationIndex)
                val album = it.getString(albumIndex)
                val artist = it.getString(artistIndex)
                val size = it.getLong(sizeIndex)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )

                list.add(
                    Audio(
                        uri = contentUri,
                        name = name,
                        createDate = convertNumberToDate(date),
                        path = path,
                        duration = duration,
                        album = album,
                        artist = artist,
                        size = size
                    )
                )
            }

            list.sortedBy { it -> it.name.lowercase() }
        }

        return list
    }
}