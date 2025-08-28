package org.zendev.arkmedia.tools

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val preferencesName = "ArkMediaPref"

val selectedItems = mutableListOf<Any>()
val selectedViews = mutableListOf<View>()
val vars = mutableMapOf<String, Any>()

fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, text)

    clipboardManager.setPrimaryClip(clipData)
}

@SuppressLint("SourceLockedOrientationActivity")
fun lockActivityOrientation(activity: Activity) {
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}
fun shareText(context: Context, title: String, text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, title)
    startActivity(context, shareIntent, null)
}

fun getDate(): String {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
}

fun convertNumberToDate(number: Long) : String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(number * 1000))
}

fun setButtonBackground(context: Context, button: Button, colorResource: Int) {
    button.backgroundTintList = ContextCompat.getColorStateList(context, colorResource)
}

fun changeTheme(value: Int) {
    when (value) {
        0 -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        1 -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        2 -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}

fun getAllViews(view: View, includeViewGroup: Boolean): MutableList<View> {
    val result = mutableListOf<View>()

    if (view is ViewGroup) {
        if (includeViewGroup) {
            result += view
        }

        for (i in 0 until view.childCount) {
            result += getAllViews(view.getChildAt(i), includeViewGroup)
        }
    } else {
        result += view
    }

    return result
}

fun startDialogAnimation(view: View) {
    var animationDuration = 75L
    val views = getAllViews(view, true)

    views.forEachIndexed { _, v ->
        animationDuration += 15

        val animator = ObjectAnimator.ofFloat(v, "translationY", 100f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
        }

        animator.start()
    }
}

fun convertSize(size: Double): String? {
    var i = 0
    var tmp = size

    if (size <= 0) {
        return size.toString()
    } else if (size < 1024) {
        return size.toString() + " B"
    }

    while (tmp > 1024) {
        tmp /= 1024.0
        i++
    }

    val dotPos = tmp.toString().indexOf(".")
    var real = tmp.toString().substring(0, dotPos)

    if ((dotPos + 3) > tmp.toString().length) {
        real = real + tmp.toString().substring(dotPos)
    } else {
        real = real + tmp.toString().substring(dotPos, dotPos + 3)
    }

    when (i) {
        1 -> return real + " KB"
        2 -> return real + " MB"
        3 -> return real + " GB"
        4 -> return real + " TB"

        else -> return null
    }
}

suspend fun getVideoDuration(context: Context, videoUri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, videoUri)

    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    retriever.release()

    return durationStr?.toLongOrNull()?: 0L
}

fun formatMilliseconds(ms: Long): String {
    val totalSeconds = ms / 1000

    val seconds = (totalSeconds % 60).toInt()
    val minutes = ((totalSeconds / 60) % 60).toInt()
    val hours = (totalSeconds / 3600).toInt()

    if (hours == 0) {
        return String.format("%02d:%02d", minutes, seconds)
    } else if (minutes == 0) {
        return String.format("%02d", seconds)
    }

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun isActivityDestroyed(context: Context): Boolean {
    return if (context is Activity) {
        context.isFinishing || context.isDestroyed
    } else {
        false
    }
}