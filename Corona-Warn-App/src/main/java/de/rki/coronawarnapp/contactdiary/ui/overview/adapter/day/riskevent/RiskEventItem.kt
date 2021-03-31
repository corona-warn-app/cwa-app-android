package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataItem
import java.util.Objects

data class RiskEventItem(
    @StringRes val title: Int,
    @StringRes val body: Int,
    @DrawableRes val drawableId: Int,
    val events: List<Event>
) : DayDataItem {

    override val stableId: Long = Objects.hash(title, body, drawableId, events).toLong()

    data class Event(
        val name: String,
        @ColorRes val bulledPointColor: Int,
        @StringRes val riskInfoAddition: Int? = null
    )
}
