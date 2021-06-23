package de.rki.coronawarnapp.statistics.ui.homecards.cards

import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.util.lists.HasStableId

sealed class AllStatisticsCardItem : HasStableId

data class LocalStatisticsCardItem(val isEnabled: Boolean) : AllStatisticsCardItem() {
    override val stableId: Long = -1L
}

data class StatisticsCardItem(
    val stats: StatsItem,
    val onHelpAction: (StatsItem) -> Unit
) : HasStableId, AllStatisticsCardItem() {

    override val stableId: Long = stats.cardType.id.toLong()
}
