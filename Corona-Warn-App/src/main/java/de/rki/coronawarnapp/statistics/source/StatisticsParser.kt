package de.rki.coronawarnapp.statistics.source

import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.StatisticsOuterClass
import de.rki.coronawarnapp.statistics.AppliedVaccinationRatesStats
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.IncidenceAndHospitalizationStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.OccupiedIntensiveCareStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedCompletelyStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedOnceStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedWithBoosterStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.StatsType
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@Reusable
class StatisticsParser @Inject constructor() {

    fun parse(rawData: ByteArray): StatisticsData {
        val parsed = StatisticsOuterClass.Statistics.parseFrom(rawData)

        val mappedItems: Set<GlobalStatsItem> = parsed.keyFigureCardsList.mapNotNull { rawCard ->
            rawCard.toGlobalStatsItem()
        }.toSet()

//        val orderedItems = parsed.cardIdSequenceList.mapNotNull { cardId ->
//            mappedItems.singleOrNull { it.cardType.id == cardId }.also {
//                if (it == null) Timber.tag(TAG).w("There was no card data for ID=%d", cardId)
//            }
//        }
        return StatisticsData(items = mappedItems).also {
            Timber.tag(TAG).d("Parsed statistics data, %d cards.", it.items.size)
        }
    }

    private fun KeyFigureCardOuterClass.KeyFigureCard.toGlobalStatsItem(): GlobalStatsItem? =
        try {
            val updatedAt = Instant.ofEpochSecond(header.updatedAt)
            val keyFigures = keyFiguresList
            val type = StatsType.values().singleOrNull { it.id == header.cardId }
            when (type) {
                StatsType.INFECTION -> InfectionStats(updatedAt = updatedAt, keyFigures = keyFigures)
                StatsType.INCIDENCE_AND_HOSPITALIZATION -> IncidenceAndHospitalizationStats(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                StatsType.KEY_SUBMISSION -> KeySubmissionsStats(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                StatsType.SEVEN_DAY_RVALUE -> SevenDayRValue(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                StatsType.PERSONS_VACCINATED_ONCE -> PersonsVaccinatedOnceStats(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                StatsType.PERSONS_VACCINATED_COMPLETELY -> PersonsVaccinatedCompletelyStats(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                StatsType.APPLIED_VACCINATION_RATES -> AppliedVaccinationRatesStats(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                StatsType.OCCUPIED_INTENSIVE_CARE_BEDS -> OccupiedIntensiveCareStats(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                StatsType.PERSONS_VACCINATED_WITH_BOOSTER -> PersonsVaccinatedWithBoosterStats(
                    updatedAt = updatedAt,
                    keyFigures = keyFigures
                )

                // Unknown Global statistics and Local are ignored here
                else -> null.also { Timber.tag(TAG).e("Unknown general statistics type: %s", this) }
            }.also {
                Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                it?.requireValidity()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("Failed to parse raw card: %s", this)
            null
        }

    companion object {
        const val TAG = "StatisticsParser"
    }
}
