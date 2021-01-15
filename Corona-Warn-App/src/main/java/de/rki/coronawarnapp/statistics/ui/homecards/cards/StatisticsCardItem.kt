package de.rki.coronawarnapp.statistics.ui.homecards.cards

import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.util.lists.HasStableId

data class StatisticsCardItem(
    val stats: StatsItem,
    val onHelpAction: (StatsItem) -> Unit
) : HasStableId {

    override val stableId: Long = stats.cardType.id.toLong()
}
