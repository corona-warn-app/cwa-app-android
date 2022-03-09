package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.appconfig.CoronaPCRTestParametersContainer
import de.rki.coronawarnapp.appconfig.CoronaRapidAntigenTestParametersContainer
import de.rki.coronawarnapp.appconfig.CoronaTestConfigContainer
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant

import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class CoronaTestTest : BaseTest() {

    @Test
    fun `PCR didPassConfigDuration`() {
        // 3 hours age
        pcrTest(Instant.parse("2022-03-03T20:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(3),
                now = Instant.parse("2022-03-03T23:00:00.000Z")
            ) shouldBe true

        // 1 hour age
        pcrTest(Instant.parse("2022-03-03T20:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(1),
                now = Instant.parse("2022-03-03T21:00:00.000Z")
            ) shouldBe true

        // 1/2 hour age
        pcrTest(Instant.parse("2022-03-03T20:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(1),
                now = Instant.parse("2022-03-03T20:30:00.000Z")
            ) shouldBe false

        pcrTest(Instant.parse("2022-03-03T23:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(),
                now = Instant.parse("2022-03-08T23:00:00.000Z")
            ) shouldBe false

        pcrTest(Instant.parse("2022-03-03T23:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(),
                now = Instant.parse("2022-03-10T22:59:00.000Z")
            ) shouldBe false

        pcrTest(Instant.parse("2022-03-03T23:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(),
                now = Instant.parse("2022-03-10T23:00:00.000Z")
            ) shouldBe true
    }

    @Test
    fun `RAT didPassConfigDuration`() {
        // 3 hours age
        ratTest(Instant.parse("2022-03-03T20:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(3),
                now = Instant.parse("2022-03-03T23:00:00.000Z")
            ) shouldBe true

        // 1 hour age
        ratTest(Instant.parse("2022-03-03T20:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(1),
                now = Instant.parse("2022-03-03T21:00:00.000Z")
            ) shouldBe true

        // 1/2 hour age
        ratTest(Instant.parse("2022-03-03T20:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(1),
                now = Instant.parse("2022-03-03T20:30:00.000Z")
            ) shouldBe false

        ratTest(Instant.parse("2022-03-03T23:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(),
                now = Instant.parse("2022-03-08T23:00:00.000Z")
            ) shouldBe false

        ratTest(Instant.parse("2022-03-03T23:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(),
                now = Instant.parse("2022-03-10T22:59:00.000Z")
            ) shouldBe false

        ratTest(Instant.parse("2022-03-03T23:00:00.000Z"))
            .didPassConfigDuration(
                coronaTestConfig = coronaTestParameters(),
                now = Instant.parse("2022-03-10T23:00:00.000Z")
            ) shouldBe true
    }

    private fun pcrTest(time: Instant) = mockk<PCRCoronaTest>()
        .apply { every { registeredAt } returns time }

    private fun ratTest(time: Instant) = mockk<RACoronaTest>()
        .apply { every { testTakenAt } returns time }

    private fun coronaTestParameters(hours: Long = 168) = CoronaTestConfigContainer(
        pcrParameters = CoronaPCRTestParametersContainer(
            hoursToShowRiskCard = Duration.standardHours(hours)
        ),
        ratParameters = CoronaRapidAntigenTestParametersContainer(
            hoursToShowRiskCard = Duration.standardHours(hours)
        )
    )
}
