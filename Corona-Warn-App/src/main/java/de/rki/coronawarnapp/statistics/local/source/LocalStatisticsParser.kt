package de.rki.coronawarnapp.statistics.local.source

import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure
import de.rki.coronawarnapp.server.protocols.internal.stats.LocalStatisticsOuterClass
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.LocalStatisticsData
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class LocalStatisticsParser @Inject constructor(
    private val localStatisticsConfigStorage: LocalStatisticsConfigStorage,
) {
    fun parse(rawData: ByteArray): LocalStatisticsData {
        val parsed = LocalStatisticsOuterClass.LocalStatistics.parseFrom(rawData)

        val activeSelections = localStatisticsConfigStorage.activeSelections.value.locations

        val states = activeSelections.filterIsInstance<SelectedStatisticsLocation.SelectedFederalState>()

        val mappedFederalState = parsed.federalStateDataList.mapNotNull { rawState ->
            try {
                val updatedAt = Instant.ofEpochSecond(rawState.updatedAt)
                val federalStateKeyFigure = rawState.sevenDayIncidence.toKeyFigure()

                val selectedFederalState = states.firstOrNull {
                    it.federalState.name == rawState.federalState.name
                }

                if (selectedFederalState != null) {
                    LocalIncidenceStats(
                        updatedAt = updatedAt,
                        keyFigures = listOf(federalStateKeyFigure),
                        selectedLocation = selectedFederalState
                    ).also {
                        Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                        it.requireValidity()
                    }
                } else {
                    Timber.tag(TAG).v(
                        "Failed to match federal state with id %s to user selected cards, this is probably not an error",
                        rawState.federalState.number
                    )
                    null
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse raw federal state: %s", rawState)
                null
            }
        }

        val districts = activeSelections.filterIsInstance<SelectedStatisticsLocation.SelectedDistrict>()

        val mappedAdministrativeUnit = parsed.administrativeUnitDataList.mapNotNull { rawState ->
            try {
                val updatedAt = Instant.ofEpochSecond(rawState.updatedAt)
                val administrativeUnitIncidenceKeyFigure = rawState.sevenDayIncidence.toKeyFigure()

                val leftPaddedShortId = rawState.administrativeUnitShortId
                    .toString()
                    .padStart(5, '0')

                val districtId = "110$leftPaddedShortId".toInt()

                val selectedDistrict = districts.firstOrNull {
                    it.district.districtId == districtId
                }

                if (selectedDistrict != null) {
                    LocalIncidenceStats(
                        updatedAt = updatedAt,
                        keyFigures = listOf(administrativeUnitIncidenceKeyFigure),
                        selectedLocation = selectedDistrict
                    ).also {
                        Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                        it.requireValidity()
                    }
                } else {
                    Timber.tag(TAG).v(
                        "Failed to match au with id %s to user selected cards, this is probably not an error",
                        rawState.administrativeUnitShortId
                    )
                    null
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse raw federal state: %s", rawState)
                null
            }
        }

        return LocalStatisticsData(items = mappedAdministrativeUnit + mappedFederalState).also {
            Timber.tag(TAG).d("Parsed local statistics data, %d cards.", it.items.size)
        }
    }

    private fun matchTrendToSemantic(trend: KeyFigure.Trend) =
        when (trend) {
            KeyFigure.Trend.INCREASING ->
                KeyFigure.TrendSemantic.NEGATIVE
            KeyFigure.Trend.UNSPECIFIED_TREND ->
                KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
            KeyFigure.Trend.STABLE ->
                KeyFigure.TrendSemantic.NEUTRAL
            KeyFigure.Trend.DECREASING ->
                KeyFigure.TrendSemantic.POSITIVE
            KeyFigure.Trend.UNRECOGNIZED ->
                KeyFigure.TrendSemantic.UNRECOGNIZED
        }

    private fun LocalStatisticsOuterClass.SevenDayIncidenceData.toKeyFigure(): KeyFigure =
        KeyFigure.newBuilder()
            .setRank(KeyFigure.Rank.PRIMARY)
            .setValue(value)
            .setDecimals(0)
            .setTrend(trend)
            .setTrendSemantic(matchTrendToSemantic(trend))
            .build()

    companion object {
        const val TAG = "StatisticsParser"
    }
}
