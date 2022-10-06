package de.rki.coronawarnapp.statistics

import com.google.common.collect.Ordering
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsProvider
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CombinedStatisticsProvider @Inject constructor(
    statisticsProvider: StatisticsProvider,
    localStatisticsProvider: LocalStatisticsProvider,
    networkStateProvider: NetworkStateProvider,
) {
    val statistics = combine(
        statisticsProvider.current,
        localStatisticsProvider.current,
        networkStateProvider.networkState.map { it.isInternetAvailable }.distinctUntilChanged()
    ) { statsData, localStatsData, isInternetAvailable ->
        val cardIdSequence = statsData.cardIdSequence
        val ordering = Ordering.explicit(cardIdSequence.toList())
        val stats = localStatsData.items.plus(statsData.items)
            .filterIsInstance<StatsSequenceItem>()
            .filter { it.cardType.id in cardIdSequence }
            .sortedWith { a, b ->
                ordering.compare(a.cardType.id, b.cardType.id)
            }

        val addStatsItems = setOf(
            AddStatsItem(
                canAddItem = localStatsData.items.size < 5,
                isInternetAvailable = isInternetAvailable
            )
        )
        statsData.copy(items = addStatsItems + stats)
    }
}
