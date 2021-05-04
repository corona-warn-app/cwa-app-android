package de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.separator

import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.CategoryItem

object TraceLocationSeparatorItem : CategoryItem {
    override val stableId = this.hashCode().toLong()
}
