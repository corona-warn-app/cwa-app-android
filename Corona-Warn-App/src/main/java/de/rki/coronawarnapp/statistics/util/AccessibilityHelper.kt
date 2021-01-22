package de.rki.coronawarnapp.statistics.util

import android.content.Context
import android.text.SpannableString
import android.text.style.LocaleSpan
import de.rki.coronawarnapp.contactdiary.util.getLocale

/**
 * returns localized spannable string so that screen readers read out decimal values appropriately
 */
fun getLocalizedSpannableString(context: Context, source: String) = SpannableString(source).apply {
    setSpan(LocaleSpan(context.getLocale()), 0, this.length, 0)
}
