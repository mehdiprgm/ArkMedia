package org.zendev.arkmedia.data.media

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import com.google.common.math.LongMath
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    var uri: Uri,
    var name: String,
    var path: String,
    var size: Long,
    var createDate: String,
    var duration: Long,
    var width: Int,
    var height: Int
) : Parcelable
