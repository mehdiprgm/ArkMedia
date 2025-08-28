package org.zendev.arkmedia.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import org.zendev.arkmedia.data.media.Image
import org.zendev.arkmedia.data.media.Video
import org.zendev.arkmedia.data.repository.AudioRepository
import org.zendev.arkmedia.data.repository.ImageRepository
import org.zendev.arkmedia.data.repository.VideoRepository
import org.zendev.arkmedia.data.viewmodel.MediaViewModel
import org.zendev.arkmedia.data.viewmodel.MediaViewModelFactory
import org.zendev.arkmedia.databinding.ActivitySearchBinding
import org.zendev.arkmedia.tools.getAllViews

class SearchActivity : AppCompatActivity() {
    private lateinit var b: ActivitySearchBinding
    private lateinit var viewModel: MediaViewModel

    private var mediaIndex = 0

    private lateinit var images: MutableList<Image>
    private lateinit var videos: MutableList<Video>
    private lateinit var audios: MutableList<Audio>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadMediaIndex()
        setupViewModel()
        setupSearchListener()

        loadAllMedia()
        loadMedia()
    }

    private fun loadMediaIndex() {
        mediaIndex = intent.getIntExtra("MediaIndex", 0)
    }

    private fun setupViewModel() {
        val imageRepository = ImageRepository(this)
        val videoRepository = VideoRepository(this)
        val audioRepository = AudioRepository(this)

        val factory = MediaViewModelFactory(imageRepository, videoRepository, audioRepository)
        viewModel = ViewModelProvider(this, factory)[MediaViewModel::class.java]
    }

    private fun setupSearchListener() {
        b.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val text = b.txtSearch.text.toString().lowercase()

                when (mediaIndex) {
                    0 -> {
                        loadImageFiles(
                            images.filter {
                                it.name.lowercase().contains(text)
                            }.toMutableList()
                        )
                    }

                    1 -> {
                        loadVideoFiles(
                            videos.filter {
                                it.name.lowercase().contains(text)
                            }.toMutableList()
                        )
                    }

                    2 -> {
                        loadAudioFiles(
                            audios.filter {
                                it.name.lowercase().contains(text)
                            }.toMutableList()
                        )
                    }
                }
            }
        })
    }

    private fun loadAllMedia() {
        images = viewModel.getAllImages()
        videos = viewModel.getAllVideos()
        audios = viewModel.getAllAudios()
    }

    private fun loadMedia() {
        when (mediaIndex) {
            0 -> {
                loadImageFiles(images)
            }

            1 -> {
                loadVideoFiles(videos)
            }

            2 -> {
                loadAudioFiles(audios)
            }
        }
    }

    private fun loadImageFiles(images: MutableList<Image>) {
        val adapter = ImagesAdapter(this, images)
        showEmptyList(images.isEmpty())

        b.rcItems.adapter = adapter
        b.rcItems.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter.setOnItemClickListener(object : ImagesAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View, image: Image
            ) {
                val intent = Intent(this@SearchActivity, ImageActivity::class.java)
                intent.putExtra("Image", image)

                startActivity(intent)
            }

            override fun onItemLongClick(view: View, image: Image) {
            }
        })
    }

    private fun loadVideoFiles(videos: MutableList<Video>) {
        val adapter = VideosAdapter(this, videos)
        showEmptyList(videos.isEmpty())

        b.rcItems.adapter = adapter
        b.rcItems.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : VideosAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View, video: Video
            ) {
                val intent = Intent(this@SearchActivity, VideoActivity::class.java)
                intent.putExtra("Video", video)

                startActivity(intent)
            }

            override fun onItemLongClick(view: View, video: Video) {
            }
        })
    }

    private fun loadAudioFiles(audios: MutableList<Audio>) {
        val adapter = AudioAdapter(this, audios)
        showEmptyList(audios.isEmpty())

        b.rcItems.adapter = adapter
        b.rcItems.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : AudioAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View, audio: Audio
            ) {
                var imageView: ShapeableImageView? = null
                getAllViews(view, false).forEach {
                    if (it is ShapeableImageView) {
                        imageView = it
                    }
                }

                lifecycleScope.launch {
                    imageView?.setImageResource(R.drawable.ic_pause)
                    Dialogs.audio(this@SearchActivity, audio)
                    imageView?.setImageResource(R.drawable.ic_play2)
                }
            }

            override fun onItemLongClick(view: View, audio: Audio) {
            }
        })
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
}