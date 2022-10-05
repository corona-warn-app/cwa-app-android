package de.rki.coronawarnapp.statistics

import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import timber.log.Timber
import java.time.Instant

data class StatisticsData(
    val items: List<GenericStatsItem> = emptyList()
) {
    val isDataAvailable: Boolean = items
        .isNotEmpty()

    override fun toString(): String {
        return "StatisticsData(cards=${
        items.map {
            when (it) {
                is AddStatsItem -> "AddCard(${it.isEnabled})"
                is GlobalStatsItem -> it.cardType.name + " " + it.updatedAt
                is LocalStatsItem -> it.cardType.name + " " + it.updatedAt
            }
        }
        })"
    }
}

data class LocalStatisticsData(
    val items: List<LocalIncidenceAndHospitalizationStats> = emptyList()
) {
    val isDataAvailable: Boolean = items.isNotEmpty()

    override fun toString(): String {
        return "StatisticsData(cards=${
        items.map {
            it.cardType.name + " " + it.updatedAt
        }
        })"
    }
}

sealed class GenericStatsItem

data class AddStatsItem(
    val canAddItem: Boolean,
    val isInternetAvailable: Boolean
) : GenericStatsItem() {
    val isEnabled: Boolean get() = canAddItem && isInternetAvailable
}

sealed class GlobalStatsItem(val cardType: Type) : GenericStatsItem() {
    abstract val updatedAt: Instant
    abstract val keyFigures: List<KeyFigure>

    enum class Type(val id: Int) {
        INFECTION(1),
        KEYSUBMISSION(3),
        SEVEN_DAY_RVALUE(4),
        PERSONS_VACCINATED_ONCE(5),
        PERSONS_VACCINATED_COMPLETELY(6),
        APPLIED_VACCINATION_RATES(7),
        OCCUPIED_INTENSIVE_CARE_BEDS(9),
        INCIDENCE_AND_HOSPITALIZATION(10),
        PERSONS_VACCINATED_WITH_BOOSTER(11)
    }

    abstract fun requireValidity()
}

sealed class LocalStatsItem(val cardType: Type) : GenericStatsItem() {
    abstract val updatedAt: Instant
    abstract val keyFigures: List<KeyFigure>

    enum class Type(val id: Int) {
        LOCAL_INCIDENCE(8)
    }

    abstract fun requireValidity()
}

data class InfectionStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.INFECTION) {

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
            Timber.w("InfectionStats is missing tertiary value")
        }
    }
}

data class IncidenceAndHospitalizationStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.INCIDENCE_AND_HOSPITALIZATION) {

    val sevenDayIncidence: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val sevenDayIncidenceSecondary: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.SECONDARY }

    override fun requireValidity() {
        require(keyFigures.size == 2)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("Global Incidence And Hospitalization Stats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.SECONDARY }) {
            Timber.w("Global Incidence And Hospitalization Stats is missing secondary value")
        }
    }
}

data class LocalIncidenceAndHospitalizationStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>,
    val hospitalizationUpdatedAt: Instant,
    val selectedLocation: SelectedStatisticsLocation,
) : LocalStatsItem(cardType = Type.LOCAL_INCIDENCE) {

    val sevenDayIncidence: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val sevenDayHospitalization: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.SECONDARY }

    override fun requireValidity() {
        require(keyFigures.isNotEmpty())
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("LocalIncidenceAndHospitalizationStats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.SECONDARY }) {
            Timber.w("LocalIncidenceAndHospitalizationStats is missing secondary value")
        }
    }
}

data class KeySubmissionsStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.KEYSUBMISSION) {

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
            Timber.w("KeySubmissionsStats is missing tertiary value")
        }
    }
}

data class SevenDayRValue(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.SEVEN_DAY_RVALUE) {

    val reproductionNumber: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    override fun requireValidity() {
        require(keyFigures.size == 1)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("SevenDayRValue is missing primary value")
        }
    }
}

data class PersonsVaccinatedOnceStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.PERSONS_VACCINATED_ONCE) {

    val firstDose: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val total: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.TERTIARY }

    override fun requireValidity() {
        require(keyFigures.size == 2)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("PersonsVaccinatedOnceStats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.TERTIARY }) {
            Timber.w("PersonsVaccinatedOnceStats is missing tertiary value")
        }
    }
}

data class PersonsVaccinatedCompletelyStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.PERSONS_VACCINATED_COMPLETELY) {

    val allDoses: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val total: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.TERTIARY }

    override fun requireValidity() {
        require(keyFigures.size == 2)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("PersonsVaccinatedCompletelyStats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.TERTIARY }) {
            Timber.w("PersonsVaccinatedCompletelyStats is missing tertiary value")
        }
    }
}

data class PersonsVaccinatedWithBoosterStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.PERSONS_VACCINATED_WITH_BOOSTER) {

    val boosterDoses: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val total: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.TERTIARY }

    override fun requireValidity() {
        require(keyFigures.size == 2)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("PersonsVaccinatedWithBoostersStats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.TERTIARY }) {
            Timber.w("PersonsVaccinatedWithBoosterStats is missing tertiary value")
        }
    }
}

data class AppliedVaccinationRatesStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.APPLIED_VACCINATION_RATES) {

    val administeredDoses: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    val sevenDayAverage: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.SECONDARY }

    val total: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.TERTIARY }

    override fun requireValidity() {
        require(keyFigures.size == 3)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("AppliedVaccinationRatesStats is missing primary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.SECONDARY }) {
            Timber.w("AppliedVaccinationRatesStats is missing secondary value")
        }
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.TERTIARY }) {
            Timber.w("AppliedVaccinationRatesStats is missing tertiary value")
        }
    }
}

data class OccupiedIntensiveCareStats(
    override val updatedAt: Instant,
    override val keyFigures: List<KeyFigure>
) : GlobalStatsItem(cardType = Type.OCCUPIED_INTENSIVE_CARE_BEDS) {
    val occupationRatio: KeyFigure
        get() = keyFigures.single { it.rank == KeyFigure.Rank.PRIMARY }

    override fun requireValidity() {
        require(keyFigures.size == 1)
        requireNotNull(keyFigures.singleOrNull { it.rank == KeyFigure.Rank.PRIMARY }) {
            Timber.w("OccupiedIntensiveCareStats is missing primary value")
        }
    }
}
