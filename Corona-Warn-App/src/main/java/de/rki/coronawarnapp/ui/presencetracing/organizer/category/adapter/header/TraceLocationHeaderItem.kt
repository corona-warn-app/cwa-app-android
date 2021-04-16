package de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.header

import androidx.annotation.StringRes
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.CategoryItem

data class TraceLocationHeaderItem(@StringRes val headerText: Int) : CategoryItem {
    override val stableId = this.hashCode().toLong()
}
