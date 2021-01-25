package de.rki.coronawarnapp.util

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.TextView
import androidx.annotation.StringRes

fun TextView.convertToHyperlink(url: String) {
    setText(
        SpannableString(text).apply { setSpan(URLSpan(url), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) },
        TextView.BufferType.SPANNABLE
    )
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.setUrlText(@StringRes textRes: Int, url: String) {
    context.getString(textRes, url).also {
        val indexOf = it.indexOf(url)
        setText(
            SpannableString(it).apply {
                setSpan(
                    URLSpan(url),
                    indexOf,
                    indexOf + url.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            },
            TextView.BufferType.SPANNABLE
        )
        movementMethod = LinkMovementMethod.getInstance()
    }
}

fun TextView.setUrl(@StringRes textRes: Int, label: String, url: String) {
    context.getString(textRes).also {
        val indexOf = it.indexOf(label)
        if (indexOf > 0) {
            setText(
                SpannableString(it).apply {
                    setSpan(
                        URLSpan(url),
                        indexOf,
                        indexOf + label.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                },
                TextView.BufferType.SPANNABLE
            )
            movementMethod = LinkMovementMethod.getInstance()
        } else {
            text = it
        }
    }
}

fun TextView.setUrl(@StringRes textRes: Int, @StringRes labelRes: Int, @StringRes urlRes: Int) {
    val url = context.getString(urlRes)
    val label = context.getString(labelRes)
    context.getString(textRes).also {
        val indexOf = it.indexOf(label)
        if (indexOf > 0) {
            setText(
                SpannableStringBuilder(it).urlSpan(indexOf, indexOf + label.length, url),
                TextView.BufferType.SPANNABLE
            )
            movementMethod = LinkMovementMethod.getInstance()
        } else {
            text = it
        }
    }
}
