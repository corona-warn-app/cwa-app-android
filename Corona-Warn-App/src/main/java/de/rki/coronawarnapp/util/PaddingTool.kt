package de.rki.coronawarnapp.util

object PaddingTool {
    fun requestPadding(length: Int): String = (1..length)
        .map { PADDING_ITEMS.random() }
        .joinToString("")

    private val PADDING_ITEMS = ('A'..'Z') + ('a'..'z') + ('0'..'9')
}
