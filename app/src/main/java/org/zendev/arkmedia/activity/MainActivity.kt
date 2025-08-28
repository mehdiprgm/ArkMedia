package org.zendev.arkmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import org.zendev.arkmedia.Dialogs
import org.zendev.arkmedia.R
import org.zendev.arkmedia.activity.media.ImageActivity
import org.zendev.arkmedia.activity.media.VideoActivity
import org.zendev.arkmedia.adapter.AudioAdapter
import org.zendev.arkmedia.adapter.ImagesAdapter
import org.zendev.arkmedia.adapter.VideosAdapter
import org.zendev.arkmedia.data.media.Audio
import org.zendev.arkmedia.data.viewmodel.MediaViewModel
import org.zendev.arkmedia.data.media.Image
import org.zendev.arkmedia.data.media.MediaType
import org.zendev.arkmedia.data.media.Video
import org.zendev.arkmedia.data.repository.AudioRepository
import org.zendev.arkmedia.data.repository.ImageRepository
import org.zendev.arkmedia.data.repository.VideoRepository
import org.zendev.arkmedia.data.viewmodel.MediaViewModelFactory
import org.zendev.arkmedia.databinding.ActivityMainBinding
import org.zendev.arkmedia.tools.changeTheme
import org.zendev.arkmedia.tools.getAllViews
import org.zendev.arkmedia.tools.preferencesName
import org.zendev.arkmedia.tools.vars

class MainActivity : AppCompatActivity(), View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private lateinit var b: ActivityMainBinding
    private lateinit var viewModel: MediaViewModel

    private val PERMISSION_IMAGE = 1001
    private val PERMISSION_VIDEO = 1002
    private val PERMISSION_AUDIO = 1003

    private var mediaIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadTheme()
        setupViewModel()

        b.btnMenu.setOnClickListener(this)
        b.btnSearch.setOnClickListener(this)

        b.imgGallery.setOnClickListener(this)
        b.imgVideo.setOnClickListener(this)
        b.imgAudio.setOnClickListener(this)

        mediaIndex = savedInstanceState?.getInt("MediaIndex") ?: 0
        loadMedia(mediaIndex)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("MediaIndex", mediaIndex)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnMenu -> {
                showMenu(view)
            }

            R.id.btnSearch -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("MediaIndex", mediaIndex)

                startActivity(intent)
            }

            R.id.imgGallery -> {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    loadImageFiles()
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_IMAGE
                    )
                }
            }

            R.id.imgVideo -> {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    loadVideoFiles()
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_MEDIA_VIDEO), PERMISSION_VIDEO
                    )
                }
            }

            R.id.imgAudio -> {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_MEDIA_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    loadAudioFiles()
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), PERMISSION_AUDIO
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_IMAGE -> {
                loadMedia(grantResults, MediaType.Image)
            }

            PERMISSION_VIDEO -> {
                loadMedia(grantResults, MediaType.Video)
            }

            PERMISSION_AUDIO -> {
                loadMedia(grantResults, MediaType.Audio)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuTheme -> {
                lifecycleScope.launch {
                    val newThemeValue =
                        Dialogs.theme(this@MainActivity, vars["Theme"].toString().toInt())

                    updateTheme(newThemeValue)
                }
            }

            R.id.menuRefresh -> {
                loadMedia(mediaIndex)
            }

            R.id.menuAboutArkMedia -> {
                Dialogs.aboutArkMedia(this)
            }
        }

        return true
    }

    /* check the permissions, if all permissions granted load the media by it's type *//* use this function when you request a permission and want to check the result and load media */
    private fun loadMedia(results: IntArray, mediaType: MediaType) {
        when (mediaType) {
            MediaType.Image -> {
                if (results.all { it == PackageManager.PERMISSION_GRANTED }) {
                    loadImageFiles()
                } else {
                    Dialogs.confirm(
                        this,
                        R.drawable.ic_error,
                        "Permission denied",
                        "Application doesn't have permission to access device images."
                    )
                }
            }

            MediaType.Video -> {
                if (results.all { it == PackageManager.PERMISSION_GRANTED }) {
                    loadVideoFiles()
                } else {
                    Dialogs.confirm(
                        this,
                        R.drawable.ic_error,
                        "Permission denied",
                        "Application doesn't have permission to access device videos."
                    )
                }
            }

            MediaType.Audio -> {
                if (results.all { it == PackageManager.PERMISSION_GRANTED }) {
                    loadAudioFiles()
                } else {
                    Dialogs.confirm(
                        this,
                        R.drawable.ic_error,
                        "Permission denied",
                        "Application doesn't have permission to access device audios."
                    )
                }
            }
        }
    }

    private fun loadMedia(index: Int) {
        when (index) {
            0 -> {
                loadImageFiles()
            }

            1 -> {
                loadVideoFiles()
            }

            2 -> {
                loadAudioFiles()
            }
        }
    }

    private fun setupViewModel() {
        val imageRepository = ImageRepository(this)
        val videoRepository = VideoRepository(this)
        val audioRepository = AudioRepository(this)

        val factory = MediaViewModelFactory(imageRepository, videoRepository, audioRepository)
        viewModel = ViewModelProvider(this, factory)[MediaViewModel::class.java]
    }

    private fun loadImageFiles() {
        mediaIndex = 0
        changeBottomNavigationViewItem(b.imgGallery)

        val images = viewModel.getAllImages()
        val adapter = ImagesAdapter(this, images)
        showEmptyList(images.isEmpty())

        b.rcMain.adapter = adapter
        b.rcMain.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter.setOnItemClickListener(object : ImagesAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View, image: Image
            ) {
                val intent = Intent(this@MainActivity, ImageActivity::class.java)
                intent.putExtra("Image", image)

                startActivity(intent)
            }

            override fun onItemLongClick(view: View, image: Image) {
            }
        })
    }

    private fun loadVideoFiles() {
        mediaIndex = 1
        changeBottomNavigationViewItem(b.imgVideo)

        val videos = viewModel.getAllVideos()
        val adapter = VideosAdapter(this, videos)
        showEmptyList(videos.isEmpty())

        b.rcMain.adapter = adapter
        b.rcMain.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : VideosAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View, video: Video
            ) {
                val intent = Intent(this@MainActivity, VideoActivity::class.java)
                intent.putExtra("Video", video)

                startActivity(intent)
            }

            override fun onItemLongClick(view: View, video: Video) {
            }
        })
    }

    private fun loadAudioFiles() {
        mediaIndex = 2
        changeBottomNavigationViewItem(b.imgAudio)

        val audios = viewModel.getAllAudios()
        val adapter = AudioAdapter(this, audios)
        showEmptyList(audios.isEmpty())

        b.rcMain.adapter = adapter
        b.rcMain.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : AudioAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View, audio: Audio
            ) {
                var imageView : ShapeableImageView? = null
                getAllViews(view, false).forEach {
                    if (it is ShapeableImageView) {
                        imageView = it
                    }
                }

                lifecycleScope.launch {
                    imageView?.setImageResource(R.drawable.ic_pause)
                    Dialogs.audio(this@MainActivity, audio)
                    imageView?.setImageResource(R.drawable.ic_play2)
                }
            }

            override fun onItemLongClick(view: View, audio: Audio) {
            }
        })
    }

    private fun loadTheme() {
        val sp = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val theme = sp.getInt("Theme", 2)

        vars.put("Theme", theme)
        changeTheme(theme)
    }

    private fun updateTheme(value: Int) {
        val sp = getSharedPreferences(preferencesName, MODE_PRIVATE)
        sp.edit {
            putInt("Theme", value)
        }

        changeTheme(value)
    }

    private fun showEmptyList(visible: Boolean) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        animation.duration = 500

        if (visible) {
            b.lottieEmpty.visibility = View.VISIBLE
            b.tvEmpty.visibility = View.VISIBLE

            b.lottieEmpty.animation = animation
            b.tvEmpty.animation = animation
        } else {
            b.lottieEmpty.animation = null
            b.tvEmpty.animation = null

            b.lottieEmpty.visibility = View.GONE
            b.tvEmpty.visibility = View.GONE
        }
    }

    private fun changeBottomNavigationViewItem(view: ShapeableImageView) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        animation.duration = 300

        getAllViews(b.layNavigationBottom, true).forEach {
            /* set the background to panelBackground2 and reset the color to iconColor */

            it.setBackgroundResource(R.color.panelBackground2)
            it.animation = null

            if (it is ShapeableImageView) {
                it.setColorFilter(
                    getResourceColor(R.color.iconColor), PorterDuff.Mode.SRC_IN
                )
            }
        }

        view.setBackgroundResource(R.color.theme)
        view.setColorFilter(getResourceColor(R.color.black), PorterDuff.Mode.SRC_IN)
        view.startAnimation(animation)
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)

        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    /* this function help to reduce code to access resource color */
    private fun getResourceColor(colorResource: Int): Int {
        return ContextCompat.getColor(this, colorResource)
    }

}