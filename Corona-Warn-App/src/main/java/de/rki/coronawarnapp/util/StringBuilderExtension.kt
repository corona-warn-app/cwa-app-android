package de.rki.coronawarnapp.util

import kotlin.text.StringBuilder

object StringBuilderExtension {

    fun StringBuilder.appendWithTrailingSpace(str: String): StringBuilder = this.append(str).append(" ")
    fun StringBuilder.appendWithLineBreak(str: String): StringBuilder = this.append(str).append(" \n ")
}
