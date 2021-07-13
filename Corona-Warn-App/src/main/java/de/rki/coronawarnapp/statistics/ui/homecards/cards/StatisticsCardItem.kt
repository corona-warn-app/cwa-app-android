package de.rki.coronawarnapp.statistics.ui.homecards.cards

import de.rki.coronawarnapp.statistics.AddStatsItem
import de.rki.coronawarnapp.statistics.GenericStatsItem
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.util.lists.HasStableId

data class StatisticsCardItem(
    val stats: GenericStatsItem,
    val onClickListener: (GenericStatsItem) -> Unit,
    val onRemoveListener: (LocalIncidenceStats) -> Unit,
) : HasStableId {

    override val stableId: Long = when (stats) {
        is AddStatsItem -> AddStatsItem::class.hashCode().toLong()
        is StatsItem -> {
            if (stats is LocalIncidenceStats) {
                stats.selectedDistrict.district.districtId.toLong()
            } else {
                stats.cardType.id.toLong()
            }
        }
    }
}
