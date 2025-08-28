package org.zendev.arkmedia.adapter

import android.annotation.SuppressLint
import android.content.Context
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.zendev.arkmedia.Dialogs
import org.zendev.arkmedia.R
import org.zendev.arkmedia.data.media.Image
import org.zendev.arkmedia.databinding.ImageLayoutBinding
import org.zendev.arkmedia.tools.isActivityDestroyed
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImagesAdapter(private val context: Context, var images: List<Image>) :
    RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, image: Image)

        fun onItemLongClick(view: View, image: Image)
    }

    private var itemClickListener: OnItemClickListener? = null
    private var loadingDialog = Dialogs.load(context, "ss", "")

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ImageViewHolder {
        val binding = ImageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder, position: Int
    ) {
        val image = images[position]
        val b = holder.binding

        val slideDownAnim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideDownAnim.duration = 150

        b.layImage.animation = slideDownAnim

        CoroutineScope(Dispatchers.Main).launch {
            // loadingDialog.show()

            try {
                loadImageAsync(holder, image.uri.toString())
            } catch (e: Exception) {
            } finally {
                if (loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }
            }
        }

        b.layImage.setOnClickListener { view ->
            itemClickListener?.onItemClick(view, image)
        }

        b.layImage.setOnLongClickListener { view ->
            itemClickListener?.onItemLongClick(view, image)
            true
        }
    }

    private suspend fun loadImageAsync(holder: ImageViewHolder, url: String) =
        suspendCancellableCoroutine { cont ->
            if (!isActivityDestroyed(context)) {
                Glide.with(holder.itemView.context).load(url).centerInside().thumbnail(0.5f)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (!cont.isCompleted) {
                                cont.resumeWithException(e ?: Exception("Load failed"))
                            }

                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {

                            if (!cont.isCompleted) {
                                cont.resume(Unit)
                            }

                            return false
                        }
                    }).into(holder.binding.imgCover)
            }
        }

    override fun getItemCount(): Int {
        return images.size
    }

    class ImageViewHolder(val binding: ImageLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}