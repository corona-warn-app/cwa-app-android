package de.rki.coronawarnapp.contactdiary.util

import org.joda.time.format.DateTimeFormat
import java.util.Locale

object CWADateTimeFormatPatternFactory {

    fun Locale.shortDatePattern(): String {
        DateTimeFormat.patternForStyle("S-", this).also {
            return when {
                (this == Locale.UK || this.language.equals("pl", true)) && it.contains("yyyy").not() ->
                    it.replace("yy", "yyyy")
                this.language == "tr" -> it.replace("dd", "d")
                this.language == "bg" -> it.replace("dd", "d") + " г."
                else -> it
            }
        }
    }

}
