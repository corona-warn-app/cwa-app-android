package de.rki.coronawarnapp.statistics.ui.homecards.cards

import de.rki.coronawarnapp.statistics.AddStatsItem
import de.rki.coronawarnapp.statistics.GenericStatsItem
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.util.lists.HasStableId

data class StatisticsCardItem(
    val stats: GenericStatsItem,
    val onHelpAction: (StatsItem) -> Unit
) : HasStableId {

    override val stableId: Long = when (stats) {
        is AddStatsItem -> 0L
        is StatsItem -> stats.cardType.id.toLong()
    }
}
