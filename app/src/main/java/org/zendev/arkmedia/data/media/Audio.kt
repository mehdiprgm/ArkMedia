package org.zendev.arkmedia.data.media

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(
    var uri: Uri,
    var name: String,
    var path: String,
    var createDate: String,
    var duration: Long,
    var album: String,
    var artist: String,
    var size: Long
) : Parcelable
