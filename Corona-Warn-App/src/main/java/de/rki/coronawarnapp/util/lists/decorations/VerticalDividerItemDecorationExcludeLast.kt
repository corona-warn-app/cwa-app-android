package de.rki.coronawarnapp.util.lists.decorations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class VerticalDividerItemDecorationExcludeLast(
    context: Context
) : DividerItemDecoration(context, VERTICAL) {

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        parent.adapter?.let { adapter ->
            parent.children // Displayed children on screen
                .forEach { view ->
                    val childAdapterPosition = parent.getChildAdapterPosition(view)
                        .let { if (it == RecyclerView.NO_POSITION) return else it }
                    if (childAdapterPosition != adapter.itemCount - 1) {
                        drawable?.let {
                            val dividerDrawable = it
                            val params = view.layoutParams as RecyclerView.LayoutParams
                            val top = view.bottom + params.bottomMargin
                            val bottom = top + dividerDrawable.intrinsicHeight
                            dividerDrawable.bounds = Rect(left, top, right, bottom)
                            dividerDrawable.draw(canvas)
                        }
                    }
                }
        }
    }
}
