package de.rki.coronawarnapp.util

import kotlin.text.StringBuilder

object StringBuilderExtension {

    fun StringBuilder.appendWithWhiteSpace(str: String): StringBuilder = this.append(str).append(" ")
    fun StringBuilder.appendWithLineBreak(str: String): StringBuilder = this.append(str).append(" \n ")
}
