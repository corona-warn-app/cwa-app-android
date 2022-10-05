package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class ExposureWindowFilterTest : BaseTest() {

    @MockK lateinit var exposureWindow1: ExposureWindow
    @MockK lateinit var exposureWindow2: ExposureWindow

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `filter works`() {
        val now = Instant.parse("2020-12-28T00:00:00Z")
        every { exposureWindow1.dateMillisSinceEpoch } returns Instant.parse("2020-12-13T00:00:00Z").toEpochMilli()
        every { exposureWindow2.dateMillisSinceEpoch } returns Instant.parse("2020-12-14T00:00:00Z").toEpochMilli()
        listOf(exposureWindow1, exposureWindow2).filterByAge(
            14,
            now
        ) shouldBe listOf(exposureWindow2)
    }

    @Test
    fun `filter works 2`() {
        val now = Instant.parse("2020-12-28T23:59:59Z")
        every { exposureWindow1.dateMillisSinceEpoch } returns Instant.parse("2020-12-13T00:00:00Z").toEpochMilli()
        every { exposureWindow2.dateMillisSinceEpoch } returns Instant.parse("2020-12-14T00:00:00Z").toEpochMilli()
        listOf(exposureWindow1, exposureWindow2).filterByAge(
            14,
            now
        ) shouldBe listOf(exposureWindow2)
    }

    @Test
    fun `filter works 3`() {
        val now = Instant.parse("2020-12-28T12:00:00Z")
        every { exposureWindow1.dateMillisSinceEpoch } returns Instant.parse("2020-12-13T23:59:59Z").toEpochMilli()
        every { exposureWindow2.dateMillisSinceEpoch } returns Instant.parse("2020-12-14T17:00:00Z").toEpochMilli()
        listOf(exposureWindow1, exposureWindow2).filterByAge(
            14,
            now
        ) shouldBe listOf(exposureWindow2)
    }
}
