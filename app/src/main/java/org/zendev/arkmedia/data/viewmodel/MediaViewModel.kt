package org.zendev.arkmedia.data.viewmodel

import androidx.lifecycle.ViewModel
import org.zendev.arkmedia.data.media.Audio
import org.zendev.arkmedia.data.media.Image
import org.zendev.arkmedia.data.media.Video
import org.zendev.arkmedia.data.repository.AudioRepository
import org.zendev.arkmedia.data.repository.ImageRepository
import org.zendev.arkmedia.data.repository.VideoRepository

class MediaViewModel(
    private val imageRepository: ImageRepository,
    private val videoRepository: VideoRepository,
    private val audioRepository: AudioRepository
) : ViewModel() {

    fun getAllImages(): MutableList<Image> {
        return imageRepository.getAll()
    }

    fun getAllVideos(): MutableList<Video> {
        return videoRepository.getAll()
    }

    fun getAllAudios() : MutableList<Audio> {
        return audioRepository.getAll()
    }
}