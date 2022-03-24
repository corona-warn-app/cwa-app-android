package de.rki.coronawarnapp.coronatest.type

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestExtensionsTest : BaseTest() {

    @Test
    fun `is test older than 21 days - time changes`() {
        val test = mockk<BaseCoronaTest>().apply {
            every { registeredAt } returns Instant.EPOCH
        }

        test.isOlderThan21Days(Instant.EPOCH.plus(Duration.standardDays(21))) shouldBe false
        test.isOlderThan21Days(
            Instant.EPOCH
                .plus(Duration.standardDays(21))
                .plus(Duration.standardSeconds(1))
        ) shouldBe true
    }

    @Test
    fun `is test older than 21 days - test changes`() {
        val nowUTC = Instant.EPOCH.plus(Duration.standardDays(22))
        mockk<BaseCoronaTest>().apply {
            every { registeredAt } returns Instant.EPOCH
        }.isOlderThan21Days(nowUTC) shouldBe true

        mockk<BaseCoronaTest>().apply {
            every { registeredAt } returns Instant.EPOCH.plus(Duration.standardDays(1))
        }.isOlderThan21Days(nowUTC) shouldBe false
    }
}
