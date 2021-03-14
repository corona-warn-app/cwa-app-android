package de.rki.coronawarnapp.util

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.URLSpan

fun SpannableStringBuilder.urlSpan(start: Int, end: Int, value: String): SpannableStringBuilder {
    setSpan(URLSpan(value), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}
