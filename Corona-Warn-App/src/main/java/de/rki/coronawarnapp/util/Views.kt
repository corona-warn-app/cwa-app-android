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
 * @param leftIconRes [Int] left icon resource
 * @param rightIconRes [Int] right icon resource
 * @param colorRes [Int] background color resource
 * @param iconMarginRes [Int] icon left or right margin from item's edge
 * @param [swipeDirs] Swipe directions flags such as [ItemTouchHelper.LEFT]
 * by default right and left directions are enabled
 *
 * @param onSwipe on swipe callback. It passes item's position and swipe direction
 *
 * Usage:
 * ```
 * RecyclerView.onSwipeItem(requireContext()) { position, direction ->
 *   // Do operation here
 * }
 * ```
 */
@Suppress("LongParameterList")
fun RecyclerView.onSwipeItem(
    context: Context,
    @DrawableRes leftIconRes: Int = R.drawable.ic_delete,
    @DrawableRes rightIconRes: Int = R.drawable.ic_delete,
    @ColorRes colorRes: Int = R.color.swipeBackgroundColor,
    @DimenRes iconMarginRes: Int = R.dimen.swipe_icon_margin,
    swipeDirs: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    onSwipe: (position: Int, direction: Int) -> Unit
) {
    ItemTouchHelper(
        SwipeCallback(
            context,
            leftIconRes,
            rightIconRes,
            iconMarginRes,
            colorRes,
            swipeDirs,
            onSwipe
        )
    ).attachToRecyclerView(this)
}

/**
 * RecyclerView item swipe callback
 */
private class SwipeCallback(
    context: Context,
    @DrawableRes leftIconRes: Int,
    @DrawableRes rightIconRes: Int,
    @DimenRes iconMarginRes: Int,
    @ColorRes colorRes: Int,
    swipeDirections: Int,
    private val action: (position: Int, direction: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(
    0,
    swipeDirections
) {
    private val leftIcon = context.getDrawableCompat(leftIconRes)!!
    private val rightIcon = context.getDrawableCompat(rightIconRes)!!
    private val iconMargin = context.resources.getDimensionPixelSize(iconMarginRes)
    private val background = ColorDrawable(context.getColorCompat(colorRes))

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
        val iconHeight = rightIcon.intrinsicHeight
        val iconWidth = rightIcon.intrinsicWidth

        val iconTop = itemView.top + (itemHeight - iconHeight) / 2
        val iconLeft = itemView.right - iconMargin - iconWidth
        val iconRight = itemView.right - iconMargin
        val iconBottom = iconTop + iconHeight
        rightIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        rightIcon.draw(canvas)
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
        val iconHeight = leftIcon.intrinsicHeight
        val iconWidth = leftIcon.intrinsicWidth

        val iconTop = itemView.top + (itemHeight - iconHeight) / 2
        val iconLeft = itemView.left + iconMargin
        val iconRight = itemView.left + iconMargin + iconWidth
        val iconBottom = iconTop + iconHeight
        leftIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        leftIcon.draw(canvas)
    }

    companion object {
        private const val BACKGROUND_CORNER_OFFSET = 20
    }
}
