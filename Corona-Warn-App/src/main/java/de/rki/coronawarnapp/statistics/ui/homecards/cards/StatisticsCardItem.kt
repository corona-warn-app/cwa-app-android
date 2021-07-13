package de.rki.coronawarnapp.statistics.ui.homecards.cards

import de.rki.coronawarnapp.statistics.AddStatsItem
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.LocalStatsItem
import de.rki.coronawarnapp.util.lists.HasStableId

sealed class StatisticsCardItem : HasStableId

data class GlobalStatisticsCardItem(
    val stats: GlobalStatsItem,
    val onClickListener: (GlobalStatsItem) -> Unit,
) : StatisticsCardItem() {
    override val stableId: Long = stats.cardType.id.toLong()
}

data class AddLocalStatisticsCardItem(
    val stats: AddStatsItem,
    val onClickListener: (AddStatsItem) -> Unit,
) : StatisticsCardItem() {
    override val stableId: Long = AddStatsItem::class.hashCode().toLong()
}

data class LocalStatisticsCardItem(
    val stats: LocalStatsItem,
    val onClickListener: (LocalIncidenceStats) -> Unit,
    val onRemoveListener: (LocalIncidenceStats) -> Unit,
) : StatisticsCardItem() {
    override val stableId: Long = when (stats) {
        is LocalIncidenceStats -> stats.selectedDistrict.district.districtId.toLong()
    }
}
