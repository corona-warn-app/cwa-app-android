package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.joda.time.Duration
import org.joda.time.LocalDate

data class ListItem(
    val date: LocalDate
) {
    val data: MutableList<Data> = mutableListOf()
    var risk: Risk? = null

    data class Data(
        @DrawableRes val drawableId: Int,
        val name: String,
        val duration: Duration?,
        val attributes: List<Int>?,
        val circumstances: String?,
        val type: Type
    )

    data class Risk(
        @StringRes val title: Int,
        @StringRes val body: Int,
        @DrawableRes val drawableId: Int
    )

    enum class Type {
        LOCATION, PERSON
    }
}
