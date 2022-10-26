package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact

import androidx.annotation.DrawableRes
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataItem
import java.time.Duration

data class ContactItem(
    val data: List<Data>
) : DayDataItem {

    override val stableId: Long = data.hashCode().toLong()

    data class Data(
        @DrawableRes val drawableId: Int,
        val name: String,
        val duration: Duration?,
        val attributes: List<Int>?,
        val circumstances: String?,
        val type: Type
    )

    enum class Type {
        LOCATION, PERSON
    }
}
