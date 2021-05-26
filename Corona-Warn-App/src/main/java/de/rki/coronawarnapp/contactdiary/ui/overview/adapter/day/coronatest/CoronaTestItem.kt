package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataItem

data class CoronaTestItem(val data: List<Data>) :
    DayDataItem {

    override val stableId: Long = data.hashCode().toLong()

    data class Data(@DrawableRes val icon: Int, @StringRes val header: Int, @StringRes val body: Int)
}
