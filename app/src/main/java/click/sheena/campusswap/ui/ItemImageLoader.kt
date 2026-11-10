package click.sheena.campusswap.ui

import android.graphics.Bitmap
import android.util.LruCache
import android.widget.ImageView
import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemImageStorage
import click.sheena.campusswap.data.SampleData
import click.sheena.campusswap.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Copied images use unique file names, so replacing a photo creates a new key.
private val imageCache = object : LruCache<String, Bitmap>(
    minOf(16L * 1024 * 1024, Runtime.getRuntime().maxMemory() / 8).toInt()
) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

fun Item.displayImagePath(): String = imagePath.ifBlank {
    SampleData.items.firstOrNull { it.id == id }?.imagePath.orEmpty()
}

fun bundledItemImage(imagePath: String): Int? = when (imagePath) {
    "sample:breakfast" -> R.drawable.sample_breakfast
    "sample:tablet_stand" -> R.drawable.sample_tablet_stand
    "sample:cat" -> R.drawable.sample_cat
    "sample:bottle" -> R.drawable.sample_bottle
    "sample:camera_lens" -> R.drawable.sample_camera_lens
    "sample:movie_night" -> R.drawable.sample_movie_night
    else -> null
}

fun ImageView.loadItemImage(
    imagePath: String,
    fallbackRes: Int = R.drawable.sample_cat
) {
    val request = Any()
    setTag(R.id.item_image_request, request)

    val bundledImage = bundledItemImage(imagePath)
    if (bundledImage != null || imagePath.isBlank()) {
        setImageResource(bundledImage ?: fallbackRes)
        return
    }

    imageCache.get(imagePath)?.let {
        setImageBitmap(it)
        return
    }

    // Clear a recycled card's old photo; show the fallback only on failure.
    setImageDrawable(null)

    doOnAttach {
        if (getTag(R.id.item_image_request) !== request) return@doOnAttach
        val owner = findViewTreeLifecycleOwner() ?: return@doOnAttach

        owner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                imageCache.get(imagePath)
                    ?: ItemImageStorage.readImage(imagePath)?.also {
                        imageCache.put(imagePath, it)
                    }
            }

            if (getTag(R.id.item_image_request) !== request) {
                return@launch
            }

            if (bitmap != null) {
                setImageBitmap(bitmap)
            } else {
                setImageResource(fallbackRes)
            }
        }
    }
}
