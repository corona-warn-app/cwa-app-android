package de.rki.coronawarnapp.util

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.URLSpan
import androidx.core.text.toSpannable

fun SpannableStringBuilder.urlSpan(start: Int, end: Int, value: String): SpannableStringBuilder {
    setSpan(URLSpan(value), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}

/**
 * Creates a [Spannable] from all the elements separated using [separator]
 * and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value of [limit],
 * in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 **/
@Suppress("LongParameterList")
fun <T> Iterable<T>.joinToSpannable(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): Spannable {
    return joinTo(
        SpannableStringBuilder(),
        separator,
        prefix,
        postfix,
        limit,
        truncated,
        transform
    ).toSpannable()
}
