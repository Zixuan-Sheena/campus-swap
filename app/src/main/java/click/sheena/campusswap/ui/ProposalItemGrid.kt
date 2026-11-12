package click.sheena.campusswap.ui

import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.view.doOnLayout
import com.google.android.material.imageview.ShapeableImageView

data class ProposalImage(val itemId: String, val title: String, val imagePath: String)

fun GridLayout.showProposalImages(items: List<ProposalImage>, onItemClick: (String) -> Unit) {
    doOnLayout {
        removeAllViews()
        val gap = (4 * resources.displayMetrics.density).toInt()
        val size = (width - paddingLeft - paddingRight) / 4 - gap * 2
        items.take(8).forEachIndexed { index, item ->
            val image = ShapeableImageView(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(gap, gap, gap, gap)
                    columnSpec = GridLayout.spec(index % 4)
                    rowSpec = GridLayout.spec(index / 4)
                }
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(12 * resources.displayMetrics.density)
                    .build()
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = item.title
                isFocusable = true
                setOnClickListener { onItemClick(item.itemId) }
            }
            addView(image)
            image.loadItemImage(item.imagePath)
        }
    }
}
