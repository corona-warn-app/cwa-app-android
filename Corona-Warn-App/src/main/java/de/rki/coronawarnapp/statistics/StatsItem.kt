package de.rki.coronawarnapp.statistics

import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import org.joda.time.Instant

data class StatisticsData(
    val items: List<StatsItem> = emptyList()
) {
    val isDataAvailable: Boolean = items.isNotEmpty()

    override fun toString(): String {
        return "StatisticsData(cards=${items.map { it.cardType.name + " " + it.updatedAt }})"
    }
}

sealed class StatsItem(val cardType: Type) {
    abstract val updatedAt: Instant
    abstract val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>

    enum class Type(val id: Int) {
        INFECTION(1),
        INCIDENCE(2),
        KEYSUBMISSION(3),
        SEVEN_DAY_RVALUE(4)
    }
}

data class InfectionStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardType = Type.INFECTION)

data class IncidenceStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardType = Type.INCIDENCE)

data class KeySubmissionsStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardType = Type.KEYSUBMISSION)

data class SevenDayRValue(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigureCardOuterClass.KeyFigure>
) : StatsItem(cardType = Type.SEVEN_DAY_RVALUE)
