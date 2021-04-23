package de.rki.coronawarnapp.util.hashing

import java.util.regex.Matcher
import java.util.regex.Pattern

private val VALID_HEX: Pattern = Pattern.compile("\\p{XDigit}+")

fun String?.isSha256Hash(): Boolean {
    if (this == null) return false
    val matcher: Matcher = VALID_HEX.matcher(this)
    return length == 64 && matcher.matches()
}
