package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.server.protocols.internal.stats.CardHeaderOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.StatisticsOuterClass
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.StatisticsData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class StatisticsParserTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = StatisticsParser()

    @Test
    fun `default parsing of all types`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addAllCardIdSequence(listOf(1, 3, 2, 4))
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEYSUBMISSION_PROTO)
            addKeyFigureCards(INCIDENCE_PROTO)
            addKeyFigureCards(SEVENDAYRVALUE_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            listOf(INFECTION_STATS, KEYSUBMISSION_STATS, INCIDENCE_STATS, SEVENDAYRVALUE_STATS)
        )
    }

    @Test
    fun `handle empty statistics data`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData()
    }

    @Test
    fun `handle hidden card for which we have data`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEYSUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            listOf(KEYSUBMISSION_STATS)
        )
    }

    @Test
    fun `handle corrupt card data`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addCardIdSequence(1)
            INFECTION_PROTO.toBuilder().apply {
                removeKeyFigures(2)
            }.build().let { addKeyFigureCards(it) }
            addKeyFigureCards(KEYSUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            listOf(KEYSUBMISSION_STATS)
        )
    }

    @Test
    fun `handle duplicate card data`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addCardIdSequence(1)
            addCardIdSequence(3)
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEYSUBMISSION_PROTO)
            addKeyFigureCards(KEYSUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            listOf(KEYSUBMISSION_STATS, INFECTION_STATS, KEYSUBMISSION_STATS)
        )
    }

    @Test
    fun `handle duplicate id in card sequence without crash`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addCardIdSequence(1)
            addCardIdSequence(3)
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEYSUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            listOf(KEYSUBMISSION_STATS, INFECTION_STATS, KEYSUBMISSION_STATS)
        )
    }

    @Test
    fun `handle unknown keycard data`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)

            INFECTION_PROTO.newBuilderForType().apply {
                header = this.header.toBuilder().apply {
                    cardId = 99
                }.build()
            }.build().let { addKeyFigureCards(it) }

            addKeyFigureCards(KEYSUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            listOf(KEYSUBMISSION_STATS)
        )
    }

    @Test
    fun `handle unknown id in card sequence`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addCardIdSequence(99)
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEYSUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            listOf(KEYSUBMISSION_STATS)
        )
    }

    companion object {
        val INFECTION_PROTO = KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
            CardHeaderOuterClass.CardHeader.newBuilder().apply {
                cardId = 1
                updatedAt = 123456778890
            }.build().let { header = it }
            listOf(
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
            ).let { addAllKeyFigures(it) }
        }.build()

        val INFECTION_STATS = InfectionStats(
            updatedAt = Instant.ofEpochSecond(123456778890),
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
        )

        val INCIDENCE_PROTO = KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
            CardHeaderOuterClass.CardHeader.newBuilder().apply {
                cardId = 2
                updatedAt = 1604839761
            }.build().let { header = it }
            listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 98.9
                    decimals = 1
                    trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                    trendSemantic =
                        KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                }.build()
            ).let { addAllKeyFigures(it) }
        }.build()

        val INCIDENCE_STATS = IncidenceStats(
            updatedAt = Instant.ofEpochSecond(1604839761),
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
        )

        val KEYSUBMISSION_PROTO = KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
            CardHeaderOuterClass.CardHeader.newBuilder().apply {
                cardId = 3
                updatedAt = 0
            }.build().let { header = it }
            listOf(
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
            ).let { addAllKeyFigures(it) }
        }.build()

        val KEYSUBMISSION_STATS = KeySubmissionsStats(
            updatedAt = Instant.ofEpochSecond(0),
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

        val SEVENDAYRVALUE_PROTO = KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
            CardHeaderOuterClass.CardHeader.newBuilder().apply {
                cardId = 4
                updatedAt = 1604839761
            }.build().let { header = it }
            listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 1.04
                    decimals = 2
                    trend = KeyFigureCardOuterClass.KeyFigure.Trend.INCREASING
                    trendSemantic =
                        KeyFigureCardOuterClass.KeyFigure.TrendSemantic.NEGATIVE
                }.build()
            ).let { addAllKeyFigures(it) }
        }.build()

        val SEVENDAYRVALUE_STATS = SevenDayRValue(
            updatedAt = Instant.ofEpochSecond(1604839761),
            keyFigures = listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 1.04
                    decimals = 2
                    trend = KeyFigureCardOuterClass.KeyFigure.Trend.INCREASING
                    trendSemantic =
                        KeyFigureCardOuterClass.KeyFigure.TrendSemantic.NEGATIVE
                }.build()
            )
        )
    }
}
