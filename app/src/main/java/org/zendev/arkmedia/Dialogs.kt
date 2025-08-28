package org.zendev.arkmedia

import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.text.method.PasswordTransformationMethod
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.commons.io.FilenameUtils
import org.w3c.dom.Text
import org.zendev.arkmedia.data.media.Audio
import org.zendev.arkmedia.data.media.Image
import org.zendev.arkmedia.tools.convertSize
import org.zendev.arkmedia.tools.formatMilliseconds
import org.zendev.arkmedia.tools.startDialogAnimation
import kotlin.coroutines.resume

class Dialogs {
    companion object {

        private fun createDialog(context: Context, layoutFile: Int): Dialog {
            val dialog = Dialog(context)
            dialog.setContentView(layoutFile)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )

            dialog.setCancelable(false)
            return dialog
        }

        fun confirm(context: Context, icon: Int, title: String, message: String) {
            val dialog = createDialog(context, R.layout.dialog_confirm)
            startDialogAnimation(dialog.findViewById(R.id.main))

            val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

            tvTitle.text = title
            tvMessage.text = message

            dialog.findViewById<ImageView>(R.id.imgLogo).setImageDrawable(
                ContextCompat.getDrawable(context, icon)
            )

            dialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        fun exception(context: Context, exception: Exception) {
            val message = "${exception.message}}"
            confirm(context, R.drawable.ic_error, "Error", message)
        }

        fun load(context: Context, title: String, message: String): Dialog {
            val dialog = createDialog(context, R.layout.dialog_load)
            startDialogAnimation(dialog.findViewById(R.id.main))

            dialog.findViewById<TextView>(R.id.tvTitle).text = title
            dialog.findViewById<TextView>(R.id.tvMessage).text = message

            return dialog
        }

        suspend fun ask(context: Context, icon: Int, title: String, message: String): Boolean =
            suspendCancellableCoroutine { continuation ->
                val dialog = createDialog(
                    context,
                    R.layout.dialog_ask
                )

                startDialogAnimation(dialog.findViewById(R.id.main))

                val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
                val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

                tvTitle.text = title
                tvMessage.text = message

                dialog.findViewById<ImageView>(R.id.imgIcon).setImageDrawable(
                    ContextCompat.getDrawable(context, icon)
                )

                dialog.findViewById<Button>(R.id.btnNo).setOnClickListener {
                    continuation.resume(false)
                    dialog.dismiss()
                }

                dialog.findViewById<Button>(R.id.btnYes).setOnClickListener {
                    continuation.resume(true)
                    dialog.dismiss()
                }

                dialog.setOnCancelListener {
                    continuation.resume(false)
                }

                dialog.show()
                continuation.invokeOnCancellation {
                    dialog.dismiss()
                }
            }

        fun imageProperties(context: Context, image: Image) {
            val dialog = createDialog(context, R.layout.dialog_image_properties)
            dialog.setCancelable(true)

            startDialogAnimation(dialog.findViewById(R.id.main))

            val tvName = dialog.findViewById<TextView>(R.id.tvName)
            val tvSize = dialog.findViewById<TextView>(R.id.tvSize)
            val tvResolution = dialog.findViewById<TextView>(R.id.tvResolution)
            val tvPath = dialog.findViewById<TextView>(R.id.tvPath)

            tvName.text = image.name
            tvSize.text = convertSize(image.size.toDouble())
            tvResolution.text = "${image.width} x ${image.height}"
            tvPath.text = image.path

            dialog.show()
        }

        suspend fun theme(context: Context, defaultValue: Int): Int =
            suspendCancellableCoroutine { continuation ->
                val dialog = createDialog(
                    context,
                    R.layout.dialog_theme
                )

                dialog.setCancelable(true)
                startDialogAnimation(dialog.findViewById(R.id.main))

                val btnLight = dialog.findViewById<RadioButton>(R.id.btnLight)
                val btnDark = dialog.findViewById<RadioButton>(R.id.btnDark)
                val btnSystem = dialog.findViewById<RadioButton>(R.id.btnSystem)

                when (defaultValue) {
                    0 -> {
                        btnLight.isChecked = true
                    }

                    1 -> {
                        btnDark.isChecked = true
                    }

                    2 -> {
                        btnSystem.isChecked = true
                    }
                }

                btnLight.setOnClickListener {
                    continuation.resume(0)
                    dialog.dismiss()
                }

                btnDark.setOnClickListener {
                    continuation.resume(1)
                    dialog.dismiss()
                }

                btnSystem.setOnClickListener {
                    continuation.resume(2)
                    dialog.dismiss()
                }

                dialog.show()
            }

        fun aboutArkMedia(context: Context) {
            val dialog = createDialog(context, R.layout.dialog_about_arkmedia)
            dialog.setCancelable(true)
            startDialogAnimation(dialog.findViewById(R.id.main))

            val imgGmail = dialog.findViewById<ImageView>(R.id.imgGmail)
            val imgTelegram = dialog.findViewById<ImageView>(R.id.imgTelegram)
            val imgInstagram = dialog.findViewById<ImageView>(R.id.imgInstagram)
            val imgGithub = dialog.findViewById<ImageView>(R.id.imgGithub)

            /* bug here */
            imgGmail.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:mfcrisis2016@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "")
                }

                /* optional: restrict to Gmail app if installed */
                context.startActivity(intent)
                dialog.dismiss()
            }

            imgTelegram.setOnClickListener {
                val telegramIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://t.me/zenDEv2".toUri()/* optional: limit to Telegram app only */
                    setPackage("org.telegram.messenger")
                }

                if (telegramIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(telegramIntent)
                } else {/* fallback: open in browser if Telegram is not installed */
                    val browserIntent = Intent(Intent.ACTION_VIEW, "https://t.me/zenDEv2".toUri())
                    context.startActivity(browserIntent)
                }

                dialog.dismiss()
            }

            imgInstagram.setOnClickListener {
                val uri = "http://instagram.com/_u/mehdi.la.79".toUri()
                val instagramIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.instagram.android")
                }

                if (instagramIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(instagramIntent)
                } else {/* fallback to browser if Instagram app isn't installed */
                    val webIntent = Intent(
                        Intent.ACTION_VIEW, "http://instagram.com/mehdi.la.79".toUri()
                    )
                    context.startActivity(webIntent)
                }

                dialog.dismiss()
            }

            imgGithub.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/mehdiprgm".toUri())
                context.startActivity(intent)
            }

            dialog.show()
        }

        suspend fun audio(context: Context, audio: Audio) = suspendCancellableCoroutine { cont ->
            val dialog = createDialog(context, R.layout.dialog_audio)
            dialog.setCancelable(true)

            val tvFileName = dialog.findViewById<TextView>(R.id.tvFileName)
            val tvDuration = dialog.findViewById<TextView>(R.id.tvDuration)
            val imgPlay = dialog.findViewById<ImageView>(R.id.imgPlay)
            val seekPosition = dialog.findViewById<SeekBar>(R.id.seekPosition)

            tvFileName.text = FilenameUtils.getBaseName(audio.name)
            tvDuration.text = formatMilliseconds(audio.duration)

            val mediaPlayer = MediaPlayer().apply {
                setDataSource(context, audio.uri)
                prepare()
            }

            seekPosition.max = mediaPlayer.duration
            val handler = Handler(Looper.getMainLooper())

            val updateSeekBar = object: Runnable {
                override fun run() {
                    if (mediaPlayer.isPlaying) {
                        seekPosition.progress = mediaPlayer.currentPosition
                        handler.postDelayed(this, 200)
                    }
                }
            }

            imgPlay.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    imgPlay.setImageResource(R.drawable.ic_play)

                    mediaPlayer.pause()
                    handler.removeCallbacks(updateSeekBar)
                } else {
                    imgPlay.setImageResource(R.drawable.ic_pause)

                    mediaPlayer.start()
                    handler.post(updateSeekBar)
                }
            }

            seekPosition.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            dialog.setOnDismissListener {
                mediaPlayer.let {
                    try {
                        if (it.isPlaying) {
                            it.stop()
                        }
                    } catch (e: IllegalStateException) {}

                    it.release()
                }

                handler.removeCallbacks(updateSeekBar)
                if (cont.isActive) {
                    cont.resume(Unit)
                }
            }

            mediaPlayer.setOnCompletionListener {
                if (dialog.isShowing) dialog.dismiss()
            }

            dialog.show()
            mediaPlayer.start()

            imgPlay.setImageResource(R.drawable.ic_pause)
            handler.post(updateSeekBar)

            cont.invokeOnCancellation {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }

                mediaPlayer.release()
                handler.removeCallbacks(updateSeekBar)

                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }
    }
}