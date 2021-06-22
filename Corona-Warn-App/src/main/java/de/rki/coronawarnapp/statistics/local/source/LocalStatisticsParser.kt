package de.rki.coronawarnapp.statistics.local.source

import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.LocalStatisticsOuterClass
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.StatisticsData
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class LocalStatisticsParser @Inject constructor() {

    fun parse(rawData: ByteArray): StatisticsData {
        val parsed = LocalStatisticsOuterClass.LocalStatistics.parseFrom(rawData)

        val mappedFederalStates = parsed.federalStateDataList.mapNotNull { rawState ->
            try {
                val updatedAt = Instant.ofEpochSecond(rawState.updatedAt)
                val stateIncidenceKeyFigure = rawState.sevenDayIncidence.toKeyFigure()

                LocalIncidenceStats(updatedAt = updatedAt, keyFigures = listOf(stateIncidenceKeyFigure)).also {
                    Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                    it.requireValidity()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse raw federal state: %s", rawState)
                null
            }
        }

        val mappedAdministrativeUnit = parsed.administrativeUnitDataList.mapNotNull { rawState ->
            try {
                val updatedAt = Instant.ofEpochSecond(rawState.updatedAt)
                val administrativeUnitIncidenceKeyFigure = rawState.sevenDayIncidence.toKeyFigure()

                LocalIncidenceStats(
                    updatedAt = updatedAt,
                    keyFigures = listOf(administrativeUnitIncidenceKeyFigure)
                ).also {
                    Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                    it.requireValidity()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse raw federal state: %s", rawState)
                null
            }
        }

        val mappedItems = mappedFederalStates + mappedAdministrativeUnit

        return StatisticsData(items = mappedItems).also {
            Timber.tag(TAG).d("Parsed local statistics data, %d cards.", it.items.size)
        }
    }

    private fun LocalStatisticsOuterClass.SevenDayIncidenceData.toKeyFigure(): KeyFigureCardOuterClass.KeyFigure =
        KeyFigureCardOuterClass.KeyFigure.newBuilder()
            .setRank(KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY)
            .setValue(value)
            .setDecimals(0)
            .setTrend(trend)
            .setTrendSemantic(KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
            .build()

    companion object {
        const val TAG = "StatisticsParser"
    }
}
