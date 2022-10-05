package de.rki.coronawarnapp.statistics.ui.homecards.cards

import de.rki.coronawarnapp.statistics.AddStatsItem
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.LinkStatsItem
import de.rki.coronawarnapp.statistics.LocalIncidenceAndHospitalizationStats
import de.rki.coronawarnapp.statistics.LocalStatsItem
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

sealed class StatisticsCardItem : HasStableId, HasPayloadDiffer

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

data class LinkCardItem(
    val linkStats: LinkStatsItem,
    val onClickListener: (StatsItem) -> Unit,
    val openLink: (String) -> Unit,
) : StatisticsCardItem() {
    override val stableId: Long = linkStats.url.hashCode().toLong()
}

data class LocalStatisticsCardItem(
    val stats: LocalStatsItem,
    val onClickListener: (LocalIncidenceAndHospitalizationStats) -> Unit,
    val onRemoveListener: (LocalIncidenceAndHospitalizationStats) -> Unit,
) : StatisticsCardItem() {
    override val stableId: Long = when (stats) {
        is LocalIncidenceAndHospitalizationStats -> stats.selectedLocation.uniqueID
    }
}
