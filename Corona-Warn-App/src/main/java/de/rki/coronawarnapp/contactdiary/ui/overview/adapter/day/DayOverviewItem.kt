package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem
import org.joda.time.Duration
import org.joda.time.LocalDate

data class DayOverviewItem(
    val date: LocalDate,
    val data: List<Data>,
    val risk: Risk?,
    val onItemSelectionListener: (DayOverviewItem) -> Unit
) : DiaryOverviewItem {

    override val stableId: Long = date.hashCode().toLong()

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
        @StringRes val bodyExtended: Int? = null,
        @DrawableRes val drawableId: Int
    )

    enum class Type {
        LOCATION, PERSON
    }
}
