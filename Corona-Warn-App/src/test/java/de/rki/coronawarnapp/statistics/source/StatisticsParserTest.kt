package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.server.protocols.internal.stats.CardHeaderOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.LinkCardOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.StatisticsOuterClass
import de.rki.coronawarnapp.statistics.IncidenceAndHospitalizationStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.PandemicRadarStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.StatsType
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class StatisticsParserTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = StatisticsParser()

    @Test
    fun `default parsing of all types`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addAllCardIdSequence(listOf(1, 3, 10, 4))
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEY_SUBMISSION_PROTO)
            addKeyFigureCards(INCIDENCE_AND_HOSPITALISATION_PROTO)
            addKeyFigureCards(SEVEN_DAY_R_VALUE_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            items = setOf(
                INFECTION_STATS,
                KEY_SUBMISSION_STATS,
                INCIDENCE_AND_HOSPITALISATION_STATS,
                SEVEN_DAY_R_VALUE_STATS
            ),
            cardIdSequence = setOf(1, 3, 10, 4)
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
            addKeyFigureCards(KEY_SUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            items = setOf(INFECTION_STATS, KEY_SUBMISSION_STATS),
            cardIdSequence = setOf(3)
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
            addKeyFigureCards(KEY_SUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            items = setOf(KEY_SUBMISSION_STATS),
            cardIdSequence = setOf(3, 1)
        )
    }

    @Test
    fun `handle duplicate card data`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addCardIdSequence(1)
            addCardIdSequence(3)
            addCardIdSequence(12)
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEY_SUBMISSION_PROTO)
            addKeyFigureCards(KEY_SUBMISSION_PROTO)
            addLinkCards(LINK_CARD_VALID)
            addLinkCards(LINK_CARD_DUPLICATE)
            addLinkCards(LINK_CARD_INVALID_ID)
            addLinkCards(LINK_CARD_INVALID_URL)
            addLinkCards(LINK_CARD_INVALID_URL_2)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            items = setOf(KEY_SUBMISSION_STATS, INFECTION_STATS, KEY_SUBMISSION_STATS, LINK_CARD),
            cardIdSequence = setOf(3, 1, 3, 12)
        )
    }

    @Test
    fun `handle duplicate id in card sequence without crash`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addCardIdSequence(1)
            addCardIdSequence(3)
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEY_SUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            items = setOf(KEY_SUBMISSION_STATS, INFECTION_STATS, KEY_SUBMISSION_STATS),
            cardIdSequence = setOf(3, 1, 3)
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

            addKeyFigureCards(KEY_SUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            items = setOf(KEY_SUBMISSION_STATS),
            cardIdSequence = setOf(3)
        )
    }

    @Test
    fun `handle unknown id in card sequence`() {
        val statisticsProto = StatisticsOuterClass.Statistics.newBuilder().apply {
            addCardIdSequence(3)
            addCardIdSequence(99)
            addKeyFigureCards(INFECTION_PROTO)
            addKeyFigureCards(KEY_SUBMISSION_PROTO)
        }.build().toByteArray()
        createInstance().parse(statisticsProto) shouldBe StatisticsData(
            items = setOf(INFECTION_STATS, KEY_SUBMISSION_STATS),
            cardIdSequence = setOf(3, 99)
        )
    }

    companion object {
        val INFECTION_PROTO: KeyFigureCardOuterClass.KeyFigureCard =
            KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
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
                    }.build(),
                    KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
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
                }.build(),
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.TERTIARY
                    value = 429181.0
                    decimals = 0
                    trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                    trendSemantic =
                        KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                }.build()
            )
        )

        val INCIDENCE_AND_HOSPITALISATION_PROTO: KeyFigureCardOuterClass.KeyFigureCard =
            KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
                CardHeaderOuterClass.CardHeader.newBuilder().apply {
                    cardId = 10
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
                    }.build(),
                    KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                        rank = KeyFigureCardOuterClass.KeyFigure.Rank.SECONDARY
                        value = 1.74
                        decimals = 2
                        trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                        trendSemantic =
                            KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                    }.build()
                ).let { addAllKeyFigures(it) }
            }.build()

        val INCIDENCE_AND_HOSPITALISATION_STATS = IncidenceAndHospitalizationStats(
            updatedAt = Instant.ofEpochSecond(1604839761),
            keyFigures = listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 98.9
                    decimals = 1
                    trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                    trendSemantic =
                        KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                }.build(),
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.SECONDARY
                    value = 1.74
                    decimals = 2
                    trend = KeyFigureCardOuterClass.KeyFigure.Trend.UNSPECIFIED_TREND
                    trendSemantic =
                        KeyFigureCardOuterClass.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC
                }.build()
            )
        )

        val KEY_SUBMISSION_PROTO: KeyFigureCardOuterClass.KeyFigureCard =
            KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
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

        val KEY_SUBMISSION_STATS = KeySubmissionsStats(
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

        val SEVEN_DAY_R_VALUE_PROTO: KeyFigureCardOuterClass.KeyFigureCard =
            KeyFigureCardOuterClass.KeyFigureCard.newBuilder().apply {
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

        val SEVEN_DAY_R_VALUE_STATS = SevenDayRValue(
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

        val LINK_CARD = PandemicRadarStats(
            updatedAt = Instant.EPOCH,
            url = "https://www.rki.de"
        )

        val LINK_CARD_VALID: LinkCardOuterClass.LinkCard = LinkCardOuterClass.LinkCard.newBuilder()
            .setHeader(
                CardHeaderOuterClass.CardHeader.newBuilder()
                    .setCardId(StatsType.PANDEMIC_RADAR.id)
                    .build()
            )
            .setUrl("https://www.rki.de")
            .build()

        val LINK_CARD_DUPLICATE: LinkCardOuterClass.LinkCard = LinkCardOuterClass.LinkCard.newBuilder()
            .setHeader(
                CardHeaderOuterClass.CardHeader.newBuilder()
                    .setCardId(StatsType.PANDEMIC_RADAR.id)
                    .build()
            )
            .setUrl("https://www.cwa.de")
            .build()

        val LINK_CARD_INVALID_URL: LinkCardOuterClass.LinkCard = LinkCardOuterClass.LinkCard.newBuilder()
            .setHeader(
                CardHeaderOuterClass.CardHeader.newBuilder()
                    .setCardId(StatsType.PANDEMIC_RADAR.id)
                    .build()
            )
            .setUrl("www.rki.de")
            .build()

        val LINK_CARD_INVALID_URL_2: LinkCardOuterClass.LinkCard = LinkCardOuterClass.LinkCard.newBuilder()
            .setHeader(
                CardHeaderOuterClass.CardHeader.newBuilder()
                    .setCardId(StatsType.PANDEMIC_RADAR.id)
                    .build()
            )
            .setUrl("http://www.rki.de")
            .build()

        val LINK_CARD_INVALID_ID: LinkCardOuterClass.LinkCard = LinkCardOuterClass.LinkCard.newBuilder()
            .setHeader(
                CardHeaderOuterClass.CardHeader.newBuilder()
                    .setCardId(17)
                    .build()
            )
            .setUrl("https://www.rki.de")
            .build()
    }
}
