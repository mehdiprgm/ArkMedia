package org.zendev.arkmedia.data.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MediaType : Parcelable {
    Image,
    Video,
    Audio
}