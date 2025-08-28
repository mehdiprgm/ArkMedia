package org.zendev.arkmedia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import org.zendev.arkmedia.R
import org.zendev.arkmedia.data.media.Image
import org.zendev.arkmedia.data.media.Video
import org.zendev.arkmedia.databinding.VideoLayoutBinding
import org.zendev.arkmedia.tools.convertSize
import org.zendev.arkmedia.tools.formatMilliseconds
import org.zendev.arkmedia.tools.getAllViews
import org.zendev.arkmedia.tools.getVideoDuration
import org.zendev.arkmedia.tools.isActivityDestroyed

class VideosAdapter(private val context: Context, var videos: List<Video>) :
    RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, video: Video)

        fun onItemLongClick(view: View, video: Video)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): VideoViewHolder {
        val binding = VideoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VideoViewHolder, position: Int
    ) {
        val video = videos[position]
        val b = holder.binding

        val popInAnim = AnimationUtils.loadAnimation(context, R.anim.pop_in)
        popInAnim.duration = 150

        val slideDownAnim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideDownAnim.duration = 150
        b.layVideo.animation = slideDownAnim

        getAllViews(b.layVideo, true).forEach {
            popInAnim.duration += 40
            it.animation = popInAnim
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (!isActivityDestroyed(context)) {
                Glide.with(context).load(video.uri).centerCrop().into(b.imgCover)
            }
        }

        b.tvFileName.text = FilenameUtils.getBaseName(video.name)
        b.tvSize.text = convertSize(video.size.toDouble())
        b.tvDuration.text = formatMilliseconds(video.duration)

        b.layVideo.setOnClickListener { view ->
            itemClickListener?.onItemClick(view, video)
        }

        b.layVideo.setOnLongClickListener { view ->
            itemClickListener?.onItemLongClick(view, video)
            true
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    class VideoViewHolder(val binding: VideoLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}