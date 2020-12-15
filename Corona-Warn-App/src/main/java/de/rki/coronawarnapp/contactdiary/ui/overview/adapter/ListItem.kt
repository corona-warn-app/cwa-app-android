package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import org.joda.time.LocalDate

data class ListItem(
    val date: LocalDate
) {
    val drawableAndStrings: MutableList<DrawableAndString> = mutableListOf()
}

data class DrawableAndString(
    val drawableId: Int,
    val text: String
)
