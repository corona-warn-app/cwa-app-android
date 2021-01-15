package de.rki.coronawarnapp.contactdiary.util

import org.joda.time.format.DateTimeFormat
import java.util.Locale

object CWADateTimeFormatPatternFactory {

    fun Locale.shortDatePattern(): String {
        DateTimeFormat.patternForStyle("S-", this).fixYear().also {
            /**
             * since results differ depending on the implementation of the underlying platform,
             * we make sure that we get, what we expect
             */
            return when {
                (this == Locale.UK || this.language.equals("pl", true)) && it.contains("yyyy").not() ->
                    it.replace("yy", "yyyy")
                this.language == "tr" -> it.noLeadingZeroInDayOfMonth()
                this.language == "bg" -> (if (it.endsWith('.')) it else "$it 'г'.").noLeadingZeroInDayOfMonth()
                else -> it
            }
        }
    }

    private fun String.noLeadingZeroInDayOfMonth() = replace("dd", "d")

    private fun String.fixYear() =
        if (this.contains("y") && this.contains("yy").not()) {
            replace("y", "yyyy")
        } else {
            this
        }
}
