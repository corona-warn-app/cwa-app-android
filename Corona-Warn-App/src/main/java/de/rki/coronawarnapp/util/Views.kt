package de.rki.coronawarnapp.util

import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.widget.TextView

fun TextView.convertToHyperlink(url: String) {
    setText(
        SpannableString(text).apply { setSpan(URLSpan(url), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) },
        TextView.BufferType.SPANNABLE
    )
}
