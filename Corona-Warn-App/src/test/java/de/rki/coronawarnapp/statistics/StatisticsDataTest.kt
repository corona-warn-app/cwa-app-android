package de.rki.coronawarnapp.statistics

import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Test

class StatisticsDataTest {

    @Test
    fun `infection mapping`() {
        val stats = InfectionStats(
            updatedAt = Instant.EPOCH,
            keyFigures = listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 1.0
                }.build(),
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.SECONDARY
                    value = 2.0
                }.build(),
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.TERTIARY
                    value = 3.0
                }.build()
            )
        )
        stats.apply {
            cardType shouldBe GlobalStatsItem.Type.INFECTION
            cardType.id shouldBe 1
            newInfections.value shouldBe 1.0
            sevenDayAverage.value shouldBe 2.0
            total.value shouldBe 3.0
        }

        stats.requireValidity()

        shouldThrow<IllegalArgumentException> {
            stats.copy(keyFigures = stats.keyFigures.subList(0, 1)).requireValidity()
        }
    }

    @Test
    fun `incidence mapping`() {
        val stats = IncidenceAndHospitalizationStats(
            updatedAt = Instant.EPOCH,
            keyFigures = listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 1.0
                }.build(),
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.SECONDARY
                    value = 1.0
                }.build()
            )
        )
        stats.apply {
            cardType shouldBe GlobalStatsItem.Type.INCIDENCE_AND_HOSPITALIZATION
            cardType.id shouldBe 10
            sevenDayIncidence.value shouldBe 1.0
            sevenDayIncidenceSecondary.value shouldBe 1.0
        }

        stats.requireValidity()

        shouldThrow<IllegalArgumentException> {
            stats.copy(keyFigures = emptyList()).requireValidity()
        }
    }

    @Test
    fun `keysubmission mapping`() {
        val stats = KeySubmissionsStats(
            updatedAt = Instant.EPOCH,
            keyFigures = listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 1.0
                }.build(),
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.SECONDARY
                    value = 2.0
                }.build(),
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.TERTIARY
                    value = 3.0
                }.build()
            )
        )

        stats.apply {
            cardType shouldBe GlobalStatsItem.Type.KEYSUBMISSION
            cardType.id shouldBe 3
            keySubmissions.value shouldBe 1.0
            sevenDayAverage.value shouldBe 2.0
            total.value shouldBe 3.0
        }

        stats.requireValidity()

        shouldThrow<IllegalArgumentException> {
            stats.copy(keyFigures = stats.keyFigures.subList(0, 1)).requireValidity()
        }
    }

    @Test
    fun `7 day R value mapping`() {
        val stats = SevenDayRValue(
            updatedAt = Instant.EPOCH,
            keyFigures = listOf(
                KeyFigureCardOuterClass.KeyFigure.newBuilder().apply {
                    rank = KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY
                    value = 1.0
                }.build()
            )
        )

        stats.apply {
            cardType shouldBe GlobalStatsItem.Type.SEVEN_DAY_RVALUE
            cardType.id shouldBe 4
            reproductionNumber.value shouldBe 1.0
        }

        stats.requireValidity()

        shouldThrow<IllegalArgumentException> {
            stats.copy(keyFigures = emptyList()).requireValidity()
        }
    }
}
