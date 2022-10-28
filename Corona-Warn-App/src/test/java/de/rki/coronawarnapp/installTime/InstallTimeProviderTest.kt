package de.rki.coronawarnapp.installTime

import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

internal class InstallTimeProviderTest : BaseTest() {
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Days since installation across years`() {
        every { timeStamper.nowUTC } returns Instant.parse("2022-11-03T05:35:16.000Z")

        InstallTimeProvider(
            installTime = Instant.parse("2020-11-03T05:35:16.000Z"),
            timeStamper = timeStamper
        ).daysSinceInstallation shouldBe 730
    }

    @Test
    fun `Days since installation across months`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-12-03T05:35:16.000Z")

        InstallTimeProvider(
            installTime = Instant.parse("2020-11-03T05:35:16.000Z"),
            timeStamper = timeStamper
        ).daysSinceInstallation shouldBe 30
    }

    @Test
    fun `Days since installation across days`() {
        every { timeStamper.nowUTC } returns Instant.parse("2022-11-03T05:35:16.000Z")

        InstallTimeProvider(
            installTime = Instant.parse("2022-10-24T05:35:16.000Z"),
            timeStamper = timeStamper
        ).daysSinceInstallation shouldBe 10
    }

    @Test
    fun `Days since installation same day`() {
        every { timeStamper.nowUTC } returns Instant.parse("2022-11-03T18:35:16.000Z")

        InstallTimeProvider(
            installTime = Instant.parse("2022-11-03T05:35:16.000Z"),
            timeStamper = timeStamper
        ).daysSinceInstallation shouldBe 0
    }
}
