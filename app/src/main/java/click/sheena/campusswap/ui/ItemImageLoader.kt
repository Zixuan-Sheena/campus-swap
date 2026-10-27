package click.sheena.campusswap.ui

import android.graphics.Bitmap
import android.util.LruCache
import android.widget.ImageView
import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Copied images use unique file names, so replacing a photo creates a new key.
private val imageCache = object : LruCache<String, Bitmap>(
    minOf(16L * 1024 * 1024, Runtime.getRuntime().maxMemory() / 8).toInt()
) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

fun ImageView.loadItemImage(
    imagePath: String,
    fallbackRes: Int = R.drawable.sample_cat
) {
    val request = Any()
    setTag(R.id.item_image_request, request)

    if (imagePath.isBlank()) {
        setImageResource(fallbackRes)
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
