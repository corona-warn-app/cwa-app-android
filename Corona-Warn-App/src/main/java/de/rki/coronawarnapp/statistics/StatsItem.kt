package de.rki.coronawarnapp.statistics

import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import org.joda.time.Instant

data class StatisticsData(
    val items: List<StatsItem>
) {
    val isDataAvailable: Boolean = items.isNotEmpty()
}

sealed class StatsItem(val cardId: Int) {
    abstract val updatedAt: Instant
    abstract val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
}

data class InfectionStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardId = 1)

data class IncidenceStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardId = 2)

data class KeySubmissionsStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardId = 3)

data class ReproductionNumberStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardId = 4)
