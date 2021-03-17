package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem
import org.joda.time.LocalDate

data class DayOverviewItem(
    val date: LocalDate,
    val dayData: List<DayDataItem>,
    val onItemSelectionListener: (DayOverviewItem) -> Unit
) : DiaryOverviewItem {
    override val stableId: Long = date.hashCode().toLong()
}
