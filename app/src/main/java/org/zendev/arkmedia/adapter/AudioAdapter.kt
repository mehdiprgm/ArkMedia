package org.zendev.arkmedia.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import org.apache.commons.io.FilenameUtils
import org.zendev.arkmedia.R
import org.zendev.arkmedia.data.media.Audio
import org.zendev.arkmedia.databinding.AudioLayoutBinding
import org.zendev.arkmedia.tools.formatMilliseconds
import org.zendev.arkmedia.tools.getAllViews

class AudioAdapter(private val context: Context, var audios: List<Audio>) :
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, audio: Audio)

        fun onItemLongClick(view: View, audio: Audio)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AudioViewHolder {
        val binding = AudioLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AudioViewHolder, position: Int
    ) {
        val audio = audios[position]
        val b = holder.binding

        val popInAnim = AnimationUtils.loadAnimation(context, R.anim.pop_in)
        popInAnim.duration = 150

        val slideDownAnim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideDownAnim.duration = 150
        b.layAudio.animation = slideDownAnim

        getAllViews(b.layAudio, true).forEach {
            popInAnim.duration += 40
            it.animation = popInAnim
        }

        b.tvFileName.text = FilenameUtils.getBaseName(audio.name)
        b.tvDuration.text = formatMilliseconds(audio.duration)

        b.layAudio.setOnClickListener { view ->
            itemClickListener?.onItemClick(view, audio)
        }

        b.layAudio.setOnLongClickListener { view ->
            itemClickListener?.onItemLongClick(view, audio)
            true
        }
    }

    override fun getItemCount(): Int {
        return audios.size
    }

    class AudioViewHolder(val binding: AudioLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}