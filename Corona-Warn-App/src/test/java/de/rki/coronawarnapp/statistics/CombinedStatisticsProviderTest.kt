package de.rki.coronawarnapp.statistics

import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsProvider
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

internal class CombinedStatisticsProviderTest : BaseTest() {

    @MockK lateinit var statisticsProvider: StatisticsProvider
    @MockK lateinit var localStatisticsProvider: LocalStatisticsProvider
    @MockK lateinit var networkStateProvider: NetworkStateProvider

    private val localIncidenceAndHospitalizationStats = LocalIncidenceAndHospitalizationStats(
        Instant.EPOCH, keyFigures = emptyList(), Instant.EPOCH,
        SelectedStatisticsLocation.SelectedDistrict(
            Districts.District(),
            addedAt = Instant.EPOCH
        )
    )

    private val incidenceAndHospitalisationStats = IncidenceAndHospitalizationStats(
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

    private val sevenDayRValueStats = SevenDayRValue(
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

    private val linkStatsItem = PandemicRadarStats(
        updatedAt = Instant.now(),
        url = "https://www.rki.de"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { statisticsProvider.current } returns flowOf(
            StatisticsData(items = setOf(sevenDayRValueStats, incidenceAndHospitalisationStats))
        )

        every { localStatisticsProvider.current } returns flowOf(
            LocalStatisticsData(
                items = listOf(localIncidenceAndHospitalizationStats)
            )
        )
        every { networkStateProvider.networkState } returns flowOf(NetworkStateProvider.FallbackState)
    }

    @Test
    fun `Card sequence is empty - only add item is available`() = runTest {
        every { statisticsProvider.current } returns flowOf(StatisticsData.DEFAULT)
        instance().statistics.first().items.apply {
            size shouldBe 2
            first().shouldBeInstanceOf<AddStatsItem>()
            filterIsInstance<StatsSequenceItem>() shouldBe listOf(localIncidenceAndHospitalizationStats)
        }
    }

    @Test
    fun `Card sequence has global ids only`() = runTest {
        every { statisticsProvider.current } returns flowOf(
            StatisticsData(
                items = setOf(sevenDayRValueStats, incidenceAndHospitalisationStats),
                cardIdSequence = setOf(sevenDayRValueStats.cardType.id, incidenceAndHospitalisationStats.cardType.id)
            )
        )

        instance().statistics.first().items.apply {
            size shouldBe 3
            first().shouldBeInstanceOf<AddStatsItem>()
            filterIsInstance<StatsSequenceItem>() shouldBe listOf(sevenDayRValueStats, incidenceAndHospitalisationStats)
        }
    }

    @Test
    fun `Card sequence has global ids and local ids`() = runTest {
        every { statisticsProvider.current } returns flowOf(
            StatisticsData(
                items = setOf(sevenDayRValueStats, incidenceAndHospitalisationStats),
                cardIdSequence = setOf(
                    sevenDayRValueStats.cardType.id,
                    incidenceAndHospitalisationStats.cardType.id,
                    localIncidenceAndHospitalizationStats.cardType.id
                )
            )
        )

        instance().statistics.first().items.apply {
            size shouldBe 4
            first().shouldBeInstanceOf<AddStatsItem>()
            filterIsInstance<StatsSequenceItem>() shouldBe listOf(
                sevenDayRValueStats,
                incidenceAndHospitalisationStats,
                localIncidenceAndHospitalizationStats
            )
        }
    }

    @Test
    fun `Card sequence has global ids and local ids - ordering 1`() = runTest {
        every { statisticsProvider.current } returns flowOf(
            StatisticsData(
                items = setOf(sevenDayRValueStats, incidenceAndHospitalisationStats, linkStatsItem),
                cardIdSequence = setOf(
                    sevenDayRValueStats.cardType.id,
                    incidenceAndHospitalisationStats.cardType.id,
                    localIncidenceAndHospitalizationStats.cardType.id,
                    linkStatsItem.cardType.id
                )
            )
        )

        instance().statistics.first().items.apply {
            size shouldBe 5
            first().shouldBeInstanceOf<AddStatsItem>()
            filterIsInstance<StatsSequenceItem>() shouldBe listOf(
                sevenDayRValueStats,
                incidenceAndHospitalisationStats,
                localIncidenceAndHospitalizationStats,
                linkStatsItem
            )
        }
    }

    @Test
    fun `Card sequence has global ids and local ids - ordering 2`() = runTest {
        every { statisticsProvider.current } returns flowOf(
            StatisticsData(
                items = setOf(sevenDayRValueStats, incidenceAndHospitalisationStats, linkStatsItem),
                cardIdSequence = setOf(
                    linkStatsItem.cardType.id,
                    localIncidenceAndHospitalizationStats.cardType.id,
                    sevenDayRValueStats.cardType.id,
                    incidenceAndHospitalisationStats.cardType.id
                )
            )
        )

        instance().statistics.first().items.apply {
            size shouldBe 5
            first().shouldBeInstanceOf<AddStatsItem>()
            filterIsInstance<StatsSequenceItem>() shouldBe listOf(
                linkStatsItem,
                localIncidenceAndHospitalizationStats,
                sevenDayRValueStats,
                incidenceAndHospitalisationStats
            )
        }
    }

    @Test
    fun `Default sequence - add item and local stats are available`() = runTest {
        instance().statistics.first().items.apply {
            size shouldBe 1
            first().shouldBeInstanceOf<AddStatsItem>()
        }
    }

    @Test
    fun `Add item is disable when local stats is 5+`() = runTest {
        every { localStatisticsProvider.current } returns flowOf(
            LocalStatisticsData(
                items = listOf(
                    localIncidenceAndHospitalizationStats,
                    localIncidenceAndHospitalizationStats.copy(
                        selectedLocation = SelectedStatisticsLocation.SelectedDistrict(
                            district = Districts.District(districtId = 1),
                            Instant.now()
                        )
                    ),
                    localIncidenceAndHospitalizationStats.copy(
                        selectedLocation = SelectedStatisticsLocation.SelectedDistrict(
                            district = Districts.District(districtId = 2),
                            Instant.now()
                        )
                    ),
                    localIncidenceAndHospitalizationStats.copy(
                        selectedLocation = SelectedStatisticsLocation.SelectedDistrict(
                            district = Districts.District(districtId = 3),
                            Instant.now()
                        )
                    ),
                    localIncidenceAndHospitalizationStats.copy(
                        selectedLocation = SelectedStatisticsLocation.SelectedDistrict(
                            district = Districts.District(districtId = 4),
                            Instant.now()
                        )
                    )
                )
            )
        )
        instance().statistics.first().items.apply {
            filterIsInstance<AddStatsItem>().first().isEnabled shouldBe false
        }
    }

    fun instance() = CombinedStatisticsProvider(
        statisticsProvider = statisticsProvider,
        localStatisticsProvider = localStatisticsProvider,
        networkStateProvider = networkStateProvider,
    )
}
