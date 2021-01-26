package de.rki.coronawarnapp.statistics

import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure
import org.joda.time.Instant
import timber.log.Timber

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
    abstract val keyFigures: List<KeyFigure>

    enum class Type(val id: Int) {
        INFECTION(1),
        INCIDENCE(2),
        KEYSUBMISSION(3),
        SEVEN_DAY_RVALUE(4)
    }

    abstract fun requireValidity()
}

data class InfectionStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : StatsItem(cardType = Type.INFECTION) {

    val newInfections: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val sevenDayAverage: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.SECONDARY }

    val total: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.TERTIARY }

    override fun requireValidity() {
        require(keyFigures.size == 3)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("InfectionStats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.SECONDARY }) {
            Timber.w("InfectionStats is missing secondary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.TERTIARY }) {
            Timber.w("InfectionStats is missing secondary value")
        }
    }
}

data class IncidenceStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : StatsItem(cardType = Type.INCIDENCE) {

    val sevenDayIncidence: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    override fun requireValidity() {
        require(keyFigures.size == 1)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("IncidenceStats is missing primary value")
        }
    }
}

data class KeySubmissionsStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : StatsItem(cardType = Type.KEYSUBMISSION) {

    val keySubmissions: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val sevenDayAverage: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.SECONDARY }

    val total: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.TERTIARY }

    override fun requireValidity() {
        require(keyFigures.size == 3)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("KeySubmissionsStats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.SECONDARY }) {
            Timber.w("KeySubmissionsStats is missing secondary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.TERTIARY }) {
            Timber.w("KeySubmissionsStats is missing secondary value")
        }
    }
}

data class SevenDayRValue(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : StatsItem(cardType = Type.SEVEN_DAY_RVALUE) {

    val reproductionNumber: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    override fun requireValidity() {
        require(keyFigures.size == 1)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("SevenDayRValue is missing primary value")
        }
    }
}
