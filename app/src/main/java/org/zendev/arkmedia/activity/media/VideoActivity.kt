package org.zendev.arkmedia.activity.media

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import org.apache.commons.io.FilenameUtils
import org.zendev.arkmedia.R
import org.zendev.arkmedia.data.media.Video
import org.zendev.arkmedia.databinding.ActivityVideoBinding
import org.zendev.arkmedia.tools.formatMilliseconds

class VideoActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityVideoBinding
    private lateinit var video: Video

    private lateinit var player: ExoPlayer

    private var fullScreen = false
    private var playbackPosition: Long = 0L
    private var userPausedVideo = false

    private val handler = Handler(Looper.getMainLooper())

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            val position = player.currentPosition
            val duration = player.duration

            if (duration > 0) {
                val progress = (position * 100 / duration).toInt()
                b.seekPosition.progress = progress
            }

            /* time of video passed */
            b.tvPosition.text = formatMilliseconds(position)
            handler.postDelayed(this, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadVideo(savedInstanceState)
        setVideoPlayScaleGesture()

        b.pvMain.setOnClickListener(this)

        b.imgPlay.setOnClickListener(this)
        b.imgVolume.setOnClickListener(this)
        b.imgRotate.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        resumeVideo()
    }

    override fun onPause() {
        super.onPause()
        pauseVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.pvMain -> {
                fullScreen = !fullScreen
                hideMediaController()
            }

            R.id.imgPlay -> {
                if (player.isPlaying) {
                    pauseVideo()
                } else {
                    if (player.playbackState == Player.STATE_ENDED) {
                        player.seekTo(0)
                    }

                    b.imgPlay.setImageResource(R.drawable.ic_pause)
                    player.play()
                }
            }

            R.id.imgVolume -> {
                if (player.volume == 1F) {
                    b.imgVolume.setImageResource(R.drawable.ic_mute)
                    player.volume = 0F
                } else {
                    b.imgVolume.setImageResource(R.drawable.ic_volume)
                    player.volume = 1F
                }
            }

            R.id.imgRotate -> {
                val currentOrientation = resources.configuration.orientation

                requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                val params = b.pvMain.layoutParams
                val width = b.pvMain.width
                val height = b.pvMain.height

                params.width = height
                params.height = width

                b.pvMain.layoutParams = params
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (::player.isInitialized) {
            outState.putLong("CurrentPosition", player.currentPosition)
        }
    }

    private fun pauseVideo() {
        b.imgPlay.setImageResource(R.drawable.ic_play)
        player.pause()
    }

    private fun resumeVideo() {
        if (player.playbackState != Player.STATE_ENDED) {
            b.imgPlay.setImageResource(R.drawable.ic_pause)
            player.play()
        }
    }

    private fun loadVideo(bundle: Bundle? = null) {
        video = intent.getParcelableExtra("Video", Video::class.java)!!

        b.tvFileName.text = FilenameUtils.getBaseName(video.name)
        b.tvDuration.text = formatMilliseconds(video.duration)

        playbackPosition = bundle?.getLong("CurrentPosition") ?: 0L
        player = ExoPlayer.Builder(this).build()

        b.pvMain.player = player
        b.pvMain.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        player.setMediaItem(MediaItem.fromUri(video.uri))
        b.pvMain.post {
            player.prepare()
            player.seekTo(playbackPosition)
            player.play()
        }

        /* add listener to update seekbar position */
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    handler.post(updateSeekBarRunnable)
                } else if (state == Player.STATE_ENDED) {
                    handler.removeCallbacks(updateSeekBarRunnable)
                    b.imgPlay.setImageResource(R.drawable.ic_play)
                }
            }
        })

        /* change seekbar to change video position */
        b.seekPosition.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = player.duration
                    val newPosition = duration * progress / seekBar!!.max

                    player.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setVideoPlayScaleGesture() {
        val scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                var scale = 1f
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scale *= detector.scaleFactor
                    scale = scale.coerceIn(1f, 3f)

                    b.pvMain.scaleX = scale
                    b.pvMain.scaleY = scale

                    return true
                }
            })

        b.pvMain.setOnTouchListener { v, event ->
            scaleGestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun hideMediaController() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        fadeIn.duration = 300
        fadeOut.duration = 300

        if (fullScreen) {
            b.layMediaController.animation = fadeOut

            b.imgVolume.animation = fadeOut
            b.imgPlay.animation = fadeOut
            b.imgRotate.animation = fadeOut

            b.tvFileName.animation = fadeOut
        } else {
            b.layMediaController.animation = fadeIn

            b.imgVolume.animation = fadeIn
            b.imgPlay.animation = fadeIn
            b.imgRotate.animation = fadeIn

            b.tvFileName.animation = fadeIn
        }

        b.layMediaController.isVisible = !fullScreen
        b.imgVolume.isVisible = !fullScreen
        b.imgPlay.isVisible = !fullScreen
        b.imgRotate.isVisible = !fullScreen
        b.tvFileName.isVisible = !fullScreen

        b.imgPlay.isEnabled = !fullScreen
        b.seekPosition.isEnabled = !fullScreen

        b.imgVolume.isEnabled = !fullScreen
        b.imgPlay.isEnabled = !fullScreen
        b.imgRotate.isEnabled = !fullScreen
    }
}