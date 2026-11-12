package click.sheena.campusswap.ui

import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import click.sheena.campusswap.R
import click.sheena.campusswap.model.ItemStatus
import com.google.android.material.imageview.ShapeableImageView

data class ProposalImage(
    val itemId: String,
    val title: String,
    val imagePath: String,
    val status: ItemStatus = ItemStatus.AVAILABLE
)

fun GridLayout.showProposalImages(items: List<ProposalImage>, onItemClick: (String) -> Unit) {
    doOnLayout {
        removeAllViews()
        val gap = (4 * resources.displayMetrics.density).toInt()
        val size = (width - paddingLeft - paddingRight) / 4 - gap * 2
        items.take(8).forEachIndexed { index, item ->
            val cell = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    setMargins(gap, gap, gap, gap)
                    columnSpec = GridLayout.spec(index % 4)
                    rowSpec = GridLayout.spec(index / 4)
                }
            }
            val image = ShapeableImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(size, size)
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(12 * resources.displayMetrics.density)
                    .build()
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = item.title
                isFocusable = true
                setOnClickListener { onItemClick(item.itemId) }
            }
            cell.addView(image)
            cell.addView(TextView(context).apply {
                text = item.title
                textSize = 12f
                gravity = Gravity.CENTER
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
            })
            val statusRes = when (item.status) {
                ItemStatus.AVAILABLE -> null
                ItemStatus.RESERVED -> R.string.proposal_item_reserved
                ItemStatus.EXCHANGED -> R.string.proposal_item_exchanged
                ItemStatus.WITHDRAWN -> R.string.proposal_item_withdrawn
            }
            if (statusRes != null) {
                cell.addView(TextView(context).apply {
                    setText(statusRes)
                    textSize = 12f
                    gravity = Gravity.CENTER
                })
            }
            addView(cell)
            image.loadItemImage(item.imagePath)
        }
    }
}
