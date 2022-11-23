package de.rki.coronawarnapp.statistics.local.source

import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure
import de.rki.coronawarnapp.server.protocols.internal.stats.LocalStatisticsOuterClass
import de.rki.coronawarnapp.statistics.LocalIncidenceAndHospitalizationStats
import de.rki.coronawarnapp.statistics.LocalStatisticsData
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@Reusable
class LocalStatisticsParser @Inject constructor(
    private val localStatisticsConfigStorage: LocalStatisticsConfigStorage,
) {
    suspend fun parse(rawData: ByteArray): LocalStatisticsData {
        val parsed = LocalStatisticsOuterClass.LocalStatistics.parseFrom(rawData)

        val activeSelections = localStatisticsConfigStorage.activeSelections.first().locations

        val states = activeSelections.filterIsInstance<SelectedStatisticsLocation.SelectedFederalState>()
        parsed.federalStateDataList.size

        val mappedFederalState = parsed.federalStateDataList.mapNotNull { rawState ->
            try {
                val incidenceUpdatedAt = Instant.ofEpochSecond(rawState.updatedAt)
                val federalStateLocalIncidenceKeyFigure = rawState.sevenDayIncidence.toKeyFigure()
                val federalStateLocalHospitalizationKeyFigure =
                    rawState.sevenDayHospitalizationIncidence.toKeyFigure(rank = KeyFigure.Rank.SECONDARY)
                val hospitalizationUpdatedAt = Instant.ofEpochSecond(rawState.sevenDayHospitalizationIncidenceUpdatedAt)
                val selectedFederalState = states.firstOrNull {
                    it.federalState.name == rawState.federalState.name
                }

                if (selectedFederalState != null) {
                    LocalIncidenceAndHospitalizationStats(
                        updatedAt = incidenceUpdatedAt,
                        keyFigures = listOf(
                            federalStateLocalIncidenceKeyFigure,
                            federalStateLocalHospitalizationKeyFigure
                        ),
                        hospitalizationUpdatedAt = hospitalizationUpdatedAt,
                        selectedLocation = selectedFederalState
                    ).also {
                        Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                        it.requireValidity()
                    }
                } else {
                    Timber.tag(TAG).v(
                        "Federal State %s was in package but not selected by user",
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
                    val shortFederalStateName = selectedDistrict.district.federalStateShortName
                    val federalStateOfDistrict = parsed.federalStateDataList.firstOrNull { state ->
                        shortFederalStateName == state.federalState.name
                            .substringAfterLast('_')
                            .run { "${first()}${last()}" }
                    }
                    if (federalStateOfDistrict != null) {
                        val administrativeUnitHospitalizationKeyFigure =
                            federalStateOfDistrict.sevenDayHospitalizationIncidence.toKeyFigure(
                                KeyFigure.Rank.SECONDARY
                            )
                        val hospitalizationUpdatedAt =
                            Instant.ofEpochSecond(federalStateOfDistrict.sevenDayHospitalizationIncidenceUpdatedAt)
                        LocalIncidenceAndHospitalizationStats(
                            updatedAt = updatedAt,
                            keyFigures = listOf(
                                administrativeUnitIncidenceKeyFigure,
                                administrativeUnitHospitalizationKeyFigure
                            ),
                            hospitalizationUpdatedAt = hospitalizationUpdatedAt,
                            selectedLocation = selectedDistrict
                        ).also {
                            Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                            it.requireValidity()
                        }
                    } else {
                        throw IllegalStateException("Could not determine federal state of selected district")
                    }
                } else {
                    Timber.tag(TAG).v(
                        "Administrative Unit %s was in package but not selected by user",
                        rawState.administrativeUnitShortId
                    )
                    null
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse raw administrative unit: %s", rawState)
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

    /** For Local Statistics, ProtoBuf doesn't distinguish
     * between PRIMARY and SECONDARY keyfigures rank so we have to manually do it here
     */
    private fun LocalStatisticsOuterClass.SevenDayIncidenceData.toKeyFigure(
        rank: KeyFigure.Rank = KeyFigure.Rank.PRIMARY
    ): KeyFigure =
        KeyFigure.newBuilder()
            .setRank(rank)
            .setValue(value)
            .setDecimals(1)
            .setTrend(trend)
            .setTrendSemantic(matchTrendToSemantic(trend))
            .build()

    companion object {
        const val TAG = "StatisticsParser"
    }
}
