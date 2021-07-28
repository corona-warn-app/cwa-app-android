package de.rki.coronawarnapp.util.lists.decorations

import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class RecylerViewPaddingDecorator(
    @DimenRes val topPadding: Int? = null,
    @DimenRes val bottomPadding: Int? = null,
    @DimenRes val leftPadding: Int? = null,
    @DimenRes val rightPadding: Int? = null
) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        if (itemPosition == RecyclerView.NO_POSITION) return

        val resources = parent.context.resources

        topPadding?.let {
            outRect.top = resources.getDimensionPixelSize(it)
        }
        bottomPadding?.let {
            outRect.bottom = resources.getDimensionPixelSize(it)
        }
        leftPadding?.let {
            outRect.left = resources.getDimensionPixelSize(it)
        }
        rightPadding?.let {
            outRect.right = resources.getDimensionPixelSize(it)
        }
    }
}
