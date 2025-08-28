package org.zendev.arkmedia.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.zendev.arkmedia.data.media.Image
import org.zendev.arkmedia.data.media.MediaType
import org.zendev.arkmedia.tools.convertNumberToDate

class ImageRepository(private val context: Context) {

    fun getAll(): MutableList<Image> {
        val list = mutableListOf<Image>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val date = it.getLong(dateIndex)
                val size = it.getLong(sizeIndex)
                val width = it.getInt(widthIndex)
                val height = it.getInt(heightIndex)
                val path = it.getString(pathIndex)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                list.add(
                    Image(
                        uri = contentUri,
                        path = path,
                        name = name,
                        createDate = convertNumberToDate(date),
                        size = size,
                        width = width,
                        height = height
                    )
                )
            }

            list.sortedBy { it -> it.name.lowercase() }
        }

        return list
    }
}