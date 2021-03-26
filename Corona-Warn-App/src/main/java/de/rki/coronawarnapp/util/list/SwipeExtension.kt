package de.rki.coronawarnapp.util.list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import kotlin.math.max
import kotlin.math.min

/**
 * On [RecyclerView] item swipe listener
 * @param context [Context]
 * @param excludedPositions excluded positions from swiping factory
 * @param onSwipe on swipe callback. It passes item's position and swipe direction
 *
 * Usage:
 * ```
 * RecyclerView.onSwipeItem(
 *   context = requireContext(),
 *   excludedPositions = listOf(0, 1)
 * ) { position, direction ->
 *   // Do operation here
 * }
 * ```
 */
fun RecyclerView.onSwipeItem(
    context: Context,
    excludedPositions: List<Int> = emptyList(),
    onSwipe: (position: Int, direction: Int) -> Unit
) {
    ItemTouchHelper(
        SwipeCallback(
            context,
            excludedPositions,
            onSwipe
        )
    ).attachToRecyclerView(this)
}

/**
 * RecyclerView item swipe callback
 */
private class SwipeCallback(
    context: Context,
    private val excludedPositions: List<Int>,
    private val action: (position: Int, direction: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    private val icon = context.getDrawableCompat(R.drawable.ic_delete)!!
    private val iconMargin = context.resources.getDimensionPixelSize(R.dimen.swipe_icon_margin)
    private val radius = context.resources.getDimensionPixelSize(R.dimen.radius_card).toFloat()
    private val backgroundPaint = Paint().apply {
        color = context.getColorCompat(R.color.swipeDeleteBackgroundColor)
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        action(viewHolder.adapterPosition, direction)
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView: View = viewHolder.itemView
        when {
            // Swiping to the right
            dX > 0 -> onSwipeRight(itemView, dX, canvas)

            // Swiping to the left
            dX < 0 -> onSwipeLeft(itemView, dX, canvas)

            // View is unSwiped
            else -> canvas.drawRect(
                itemView.right.toFloat(),
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat(),
                clearPaint
            )
        }
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder.adapterPosition in excludedPositions) return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    private fun onSwipeLeft(
        itemView: View,
        dX: Float,
        canvas: Canvas
    ) {
        // Draw background
        val backgroundWidth = max(
            (itemView.right + dX.toInt() - BACKGROUND_CORNER_OFFSET).toFloat(),
            itemView.left.toFloat()
        )

        val recF = RectF(
            backgroundWidth,
            itemView.top.toFloat(),
            itemView.right.toFloat(),
            itemView.bottom.toFloat()
        )
        canvas.drawRoundRect(recF, radius, radius, backgroundPaint)

        // Draw right icon
        val itemHeight = itemView.height
        val iconHeight = icon.intrinsicHeight
        val iconWidth = icon.intrinsicWidth

        val iconTop = itemView.top + (itemHeight - iconHeight) / 2
        val iconLeft = itemView.right - iconMargin - iconWidth
        val iconRight = itemView.right - iconMargin
        val iconBottom = iconTop + iconHeight
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        icon.draw(canvas)
    }

    private fun onSwipeRight(
        itemView: View,
        dX: Float,
        canvas: Canvas
    ) {
        // Draw background
        val backgroundWidth = min(
            (itemView.left + dX.toInt() + BACKGROUND_CORNER_OFFSET).toFloat(),
            itemView.right.toFloat()
        )

        val recF = RectF(
            itemView.left.toFloat(),
            itemView.top.toFloat(),
            backgroundWidth,
            itemView.bottom.toFloat()
        )
        canvas.drawRoundRect(recF, radius, radius, backgroundPaint)

        // Draw left icon
        val itemHeight = itemView.height
        val iconHeight = icon.intrinsicHeight
        val iconWidth = icon.intrinsicWidth

        val iconTop = itemView.top + (itemHeight - iconHeight) / 2
        val iconLeft = itemView.left + iconMargin
        val iconRight = itemView.left + iconMargin + iconWidth
        val iconBottom = iconTop + iconHeight
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        icon.draw(canvas)
    }

    companion object {
        private const val BACKGROUND_CORNER_OFFSET = 20
    }
}
