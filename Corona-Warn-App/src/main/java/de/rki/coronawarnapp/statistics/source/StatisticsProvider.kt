package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.StatisticsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsProvider @Inject constructor() {

    private val currentInternal = MutableStateFlow(StatisticsData(items = emptyList()))
    val current: Flow<StatisticsData> = currentInternal.filterNotNull()

    init {
        // Mock data
        GlobalScope.launch(context = Dispatchers.IO) {
            val statisticsData = StatisticsData(
                items = listOf(
                    InfectionStats(
                        updatedAt = Instant.ofEpochMilli(1604839761),
                        keyFigures = listOf(
                            KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                                rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                                value = 14714.0
                                decimals = 0
                                trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                                trendSemantic =
                                    KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                            }.build(),
                            KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                                rank = KeyFigureCardOuterClass.KeyFigure.Rank.SECONDARY
                                value = 11981.0
                                decimals = 0
                                trend = KeyFigureCardOuterClass.KeyFigure.Trend.INCREASING
                                trendSemantic = KeyFigureCardOuterClass.KeyFigure.TrendSemantic.NEGATIVE
                            }.build(), KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                                rank = KeyFigureCardOuterClass.KeyFigure.Rank.TERTIARY
                                value = 429181.0
                                decimals = 0
                                trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                                trendSemantic =
                                    KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                            }.build()
                        )
                    ),
                    IncidenceStats(
                        updatedAt = Instant.ofEpochMilli(1604839761),
                        keyFigures = listOf(
                            KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                                rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                                value = 98.9
                                decimals = 1
                                trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                                trendSemantic =
                                    KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                            }.build()
                        )
                    ),
                    KeySubmissionsStats(
                        updatedAt = Instant.ofEpochMilli(1604839761),
                        keyFigures = listOf(
                            KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                                rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                                value = 1514.0
                                decimals = 0
                                trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                                trendSemantic =
                                    KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                            }.build(),
                            KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                                rank = KeyFigureCardOuterClass.KeyFigure.Rank.SECONDARY
                                value = 1812.0
                                decimals = 0
                                trend = KeyFigureCardOuterClass.KeyFigure.Trend.DECREASING
                                trendSemantic = KeyFigureCardOuterClass.KeyFigure.TrendSemantic.NEGATIVE
                            }.build(),
                            KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                                rank = KeyFigureCardOuterClass.KeyFigure.Rank.TERTIARY
                                value = 20922.0
                                decimals = 0
                                trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                                trendSemantic =
                                    KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                            }.build()
                        )
                    )
                )
            )
            currentInternal.emit(statisticsData)
        }
    }
}
