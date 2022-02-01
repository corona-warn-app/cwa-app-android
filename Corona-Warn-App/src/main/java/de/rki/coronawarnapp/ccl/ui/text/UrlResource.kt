package de.rki.coronawarnapp.ccl.ui.text

import java.util.Locale

fun urlResource(
    faqAnchor: String?,
    locale: Locale = Locale.getDefault()
) = lazy {
    formatFaqAnchor(faqAnchor, locale)
}

fun formatFaqAnchor(
    faqAnchor: String?,
    locale: Locale = Locale.getDefault()
) = when {
    faqAnchor.isNullOrBlank() -> null
    else -> {
        val lang = if (locale.language == Locale.GERMAN.language) locale.language else Locale.ENGLISH.language
        "https://www.coronawarn.app/$lang/faq/#$faqAnchor"
    }
}
