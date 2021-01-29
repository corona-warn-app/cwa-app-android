package de.rki.coronawarnapp.util.lists.decorations

import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class TopBottomPaddingDecorator(
    @DimenRes val topPadding: Int,
    @DimenRes val bottomPading: Int = topPadding
) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        if (itemPosition == RecyclerView.NO_POSITION) return

        val resources = parent.context.resources

        if (itemPosition == 0) {
            outRect.top = resources.getDimensionPixelSize(topPadding)
        } else {
            parent.adapter?.let {
                if (itemPosition == it.itemCount - 1) {
                    outRect.bottom = resources.getDimensionPixelSize(bottomPading)
                }
            }
        }
    }
}
