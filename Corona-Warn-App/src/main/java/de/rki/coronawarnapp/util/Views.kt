package de.rki.coronawarnapp.util

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.Patterns
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

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

fun TextView.setUrl(label: String, url: String) {
    setUrl(text, label, url)
}

fun TextView.setUrl(content: CharSequence, label: String, url: String) {
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
