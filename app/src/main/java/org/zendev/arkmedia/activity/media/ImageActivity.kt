package org.zendev.arkmedia.activity.media

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zendev.arkmedia.R
import org.zendev.arkmedia.databinding.ActivityImageBinding
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import org.zendev.arkmedia.Dialogs
import org.zendev.arkmedia.data.media.Image

class ImageActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityImageBinding
    private lateinit var image: Image

    private var REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityImageBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadImage()

        b.imgDelete.setOnClickListener(this)
        b.imgShare.setOnClickListener(this)
        b.imgCopy.setOnClickListener(this)
        b.imgProperties.setOnClickListener(this)

        b.btnBack.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnBack -> {
                finish()
            }

            R.id.imgDelete -> {
                deleteFile()
            }

            R.id.imgShare -> {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, image.uri)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Share image via"))
            }

            R.id.imgCopy -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                val clip = ClipData.newUri(contentResolver, "Image", image.uri)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this, "Image copied to the clipboard.", Toast.LENGTH_SHORT).show()
            }

            R.id.imgProperties -> {
                Dialogs.imageProperties(this, image)
            }
        }
    }

    private fun loadImage() {
        try {/* get the media from first activity */
            image = intent.getParcelableExtra("Image", Image::class.java)!!

            /* use glide to load the image into the imageview */
            Glide.with(this).load(image.uri).centerInside().into(b.imgImage)

            b.tvFileName.text = image.name
            b.tvCreateDate.text = image.createDate
        } catch (ex: Exception) {
            Dialogs.exception(this, ex)
            finish()
        }
    }

    private fun deleteFile() {
        try {/* create a delete request with file uri */
            val pendingIntent =
                MediaStore.createDeleteRequest(contentResolver, listOf(image.uri))

            startIntentSenderForResult(
                pendingIntent.intentSender, REQUEST_CODE, null, 0, 0, 0, null
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            Dialogs.exception(this, ex)
        }
    }

    /* respond to the permission request */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {/* check to see if all permissions is granted */
            val missingPermissions = grantResults.filter { it != PackageManager.PERMISSION_GRANTED }
            if (missingPermissions.isEmpty()) {
                deleteFile()
            } else {
                Dialogs.confirm(
                    this,
                    R.drawable.ic_error,
                    "Permission denied",
                    "The application doesn't have permission to delete the file"
                )
            }
        }
    }

    /* get the result from system delete activity */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {/* file deleted successfully */
                finish()
            }
        }
    }

}