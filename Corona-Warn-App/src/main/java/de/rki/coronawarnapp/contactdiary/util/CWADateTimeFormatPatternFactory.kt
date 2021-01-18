package de.rki.coronawarnapp.contactdiary.util

import org.joda.time.format.DateTimeFormat
import java.util.Locale

object CWADateTimeFormatPatternFactory {

    fun Locale.shortDatePattern() = when {
        this == Locale.GERMANY -> "dd.MM.yy"
        this == Locale.UK -> "dd/MM/yyyy"
        this == Locale.US -> "M/d/yy"
        this == Locale("bg", "BG") -> "d.MM.yy 'г'."
        this == Locale("ro", "RO") -> "dd.MM.yyyy"
        this == Locale("pl", "PL") -> "dd.MM.yyyy"
        this == Locale("tr", "TR") -> "d.MM.yyyy"
        else -> DateTimeFormat.patternForStyle("S-", this)
    }
}
