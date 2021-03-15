package de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.header

import androidx.annotation.StringRes
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.CategoryItem

data class TraceLocationHeaderItem(@StringRes val headerText: Int) : CategoryItem {
    override val stableId = this.hashCode().toLong()
}
