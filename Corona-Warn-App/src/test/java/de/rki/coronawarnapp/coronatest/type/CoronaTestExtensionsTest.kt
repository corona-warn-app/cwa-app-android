package de.rki.coronawarnapp.coronatest.type

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestExtensionsTest : BaseTest() {

    @Test
    fun `is test older than 21 days - time changes`() {
        val test = mockk<BaseCoronaTest>().apply {
            every { registeredAt } returns Instant.EPOCH
        }

        test.isOlderThan21Days(Instant.EPOCH.plus(Duration.ofDays(21))) shouldBe false
        test.isOlderThan21Days(
            Instant.EPOCH
                .plus(Duration.ofDays(21))
                .plus(Duration.ofSeconds(1))
        ) shouldBe true
    }

    @Test
    fun `is test older than 21 days - test changes`() {
        val nowJavaUTC = Instant.EPOCH.plus(Duration.ofDays(22))
        mockk<BaseCoronaTest>().apply {
            every { registeredAt } returns Instant.EPOCH
        }.isOlderThan21Days(nowJavaUTC) shouldBe true

        mockk<BaseCoronaTest>().apply {
            every { registeredAt } returns Instant.EPOCH.plus(Duration.ofDays(1))
        }.isOlderThan21Days(nowJavaUTC) shouldBe false
    }
}
