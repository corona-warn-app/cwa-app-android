package de.rki.coronawarnapp.contactdiary.util

import org.joda.time.format.DateTimeFormat
import java.util.Locale

object CWADateTimeFormatPatternFactory {

    fun Locale.shortDatePattern(): String {
        val language = language
        DateTimeFormat.patternForStyle("S-", this).normalizeYear().apply {
            /**
             * since results differ depending on the implementation of the underlying platform,
             * we make sure that we get, what we expect
             */
            return when {
                fourDigitYearRequired && !yearHas4Digits -> makeYear4Digits()
                language == "tr" -> noLeadingZeroInDayOfMonth()
                language == "bg" -> ensureYearSuffixAbbrev().noLeadingZeroInDayOfMonth()
                else -> this
            }
        }
    }

    private fun String.ensureYearSuffixAbbrev() = (if (endsWith('.')) this else "$this 'г'.")

    private val String.yearHas4Digits: Boolean
        get() = contains("yyyy")

    private fun String.makeYear4Digits() = replace("yy", "yyyy")

    private val Locale.fourDigitYearRequired: Boolean
        get() = this == Locale.UK || this.language.equals("pl", true)

    private fun String.noLeadingZeroInDayOfMonth() = replace("dd", "d")

    private fun String.normalizeYear() =
        if (this.contains("y") && this.contains("yy").not()) {
            replace("y", "yyyy")
        } else {
            this
        }
}
