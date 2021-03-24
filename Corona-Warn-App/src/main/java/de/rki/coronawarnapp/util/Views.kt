package de.rki.coronawarnapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat

fun TextView.convertToHyperlink(url: String) {
    setText(
        SpannableString(text).apply { setSpan(URLSpan(url), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) },
        TextView.BufferType.SPANNABLE
    )
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setUrl(@StringRes textRes: Int, @StringRes labelRes: Int, @StringRes urlRes: Int) {
    setUrl(context.getString(textRes), context.getString(labelRes), context.getString(urlRes))
}

fun TextView.setUrl(@StringRes textRes: Int, label: String, url: String) {
    setUrl(context.getString(textRes), label, url)
}

fun TextView.setUrl(content: String, label: String, url: String) {
    val indexOf = content.indexOf(label)
    if (indexOf > 0) {
        setText(
            SpannableStringBuilder(content).urlSpan(indexOf, indexOf + label.length, url),
            TextView.BufferType.SPANNABLE
        )
        movementMethod = LinkMovementMethod.getInstance()
    } else {
        text = content
    }
}

fun TextView.linkifyPhoneNumbers() {
    LinkifyCompat.addLinks(
        this,
        Patterns.PHONE,
        "tel:",
        Linkify.sPhoneNumberMatchFilter,
        Linkify.sPhoneNumberTransformFilter
    )
    movementMethod = LinkMovementMethod.getInstance()
    setLinkTextColor(context.getColorCompat(R.color.colorTextTint))
}

/**
 * [RecyclerView.OnScrollListener] listener wrapper
 * @param block Callback to scroll changes, passes `true` if scrolling up and `false` otherwise
 */
fun RecyclerView.onScroll(block: (Boolean) -> Unit) {
    val threshold = 50
    addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Scrolling down
                if (dy > threshold) block(false)
                // Scrolling up
                if (dy < -threshold) block(true)
                // At the top
                if (!recyclerView.canScrollVertically(-1)) block(true)
            }
        }
    )
}

/**
 * On [RecyclerView] item swipe listener
 * @param context [Context]
 * @param excludedPositions excluded positions from swiping factory
 * @param onSwipe on swipe callback. It passes item's position and swipe direction
 *
 * Usage:
 * ```
 * RecyclerView.onSwipeItem(requireContext()) { position, direction ->
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
    private val background = ColorDrawable(context.getColorCompat(R.color.swipeBackgroundColor))

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
            else -> background.setBounds(0, 0, 0, 0)
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
        background.setBounds(
            itemView.right + dX.toInt() - BACKGROUND_CORNER_OFFSET,
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(canvas)

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
        background.setBounds(
            itemView.left,
            itemView.top,
            itemView.left + dX.toInt() + BACKGROUND_CORNER_OFFSET,
            itemView.bottom
        )
        background.draw(canvas)

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
