package de.rki.coronawarnapp.util

import android.content.Context
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

fun TextView.setDoubleUrl(
    content: String,
    labelOne: String,
    urlOne: String,
    labelTwo: String,
    urlTwo: String)
{
    val indexOfOne = content.indexOf(labelOne)
    val indexOfTwo = content.indexOf(labelTwo)
    if(indexOfOne > 0 && indexOfTwo > 0)
    {
        setText(
            SpannableStringBuilder(content)
                .urlSpan(indexOfOne, indexOfOne + labelOne.length, urlOne)
                .urlSpan(indexOfTwo, indexOfTwo + labelTwo.length, urlTwo),
            TextView.BufferType.SPANNABLE
        )
        movementMethod = LinkMovementMethod.getInstance()
    } else {
        text = content
    }
}

fun TextView.setUrls(
    content: String,
    urls: List<TextViewUrlSet>
) {
    val clean = urls
        .filter { (it.label != null || it.labelResource != null) && (it.url != null || it.urlResource != null) }
        .map { it.copy().apply { index = content.indexOf(getLabel(context)!!) } }
        .filter { it.index > 0 }

    if (clean.isEmpty()) {
        text = content
        return
    }

    val stringBuilder = SpannableStringBuilder(content)
    for (set in clean) {
        with(set) {
            stringBuilder.urlSpan(index, index + label!!.length, getUrl(context)!!)
        }
    }
    setText(
        stringBuilder,
        TextView.BufferType.SPANNABLE
    )
    movementMethod = LinkMovementMethod.getInstance()
}

data class TextViewUrlSet (
    var label: String? = null,
    var url: String? = null,
    var index: Int = 0,
    @StringRes val labelResource: Int? = null,
    @StringRes val urlResource: Int? = null,
    ) {

    fun getLabel(context: Context?): String? {
        if (label == null && labelResource != null) {
            label = context?.getString(labelResource)
        }
        return label
    }

    fun getUrl(context: Context?): String? {
        if (url == null && urlResource != null) {
            url = context?.getString(urlResource)
        }
        return url
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
