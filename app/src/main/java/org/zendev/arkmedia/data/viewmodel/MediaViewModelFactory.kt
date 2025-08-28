package org.zendev.arkmedia.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.zendev.arkmedia.data.viewmodel.MediaViewModel
import org.zendev.arkmedia.data.repository.AudioRepository
import org.zendev.arkmedia.data.repository.ImageRepository
import org.zendev.arkmedia.data.repository.VideoRepository

class MediaViewModelFactory(
    private val imageRepository: ImageRepository,
    private val videoRepository: VideoRepository,
    private val audioRepository: AudioRepository
) : ViewModelProvider.Factory {

    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediaViewModel(imageRepository, videoRepository, audioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}