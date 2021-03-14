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
