package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import org.joda.time.LocalDate

data class ListItem(
    val date: LocalDate
) {
    val data: MutableList<Data> = mutableListOf()

    data class Data(
        val drawableId: Int,
        val text: String,
        val type: Type
    )

    enum class Type {
        LOCATION, PERSON
    }
}
