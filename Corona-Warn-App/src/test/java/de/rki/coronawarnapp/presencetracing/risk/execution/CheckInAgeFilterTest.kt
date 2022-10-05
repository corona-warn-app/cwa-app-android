package de.rki.coronawarnapp.presencetracing.risk.execution

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.risk.filterByAge
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toInstant
import java.time.Instant

class CheckInAgeFilterTest : BaseTest() {

    @MockK lateinit var checkIn1: CheckIn
    @MockK lateinit var checkIn2: CheckIn

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `filter works`() {
        val now = Instant.parse("2020-12-28T00:00:00Z")
        every { checkIn1.checkInEnd } returns Instant.parse("2020-12-13T00:00:00Z")
        every { checkIn2.checkInEnd } returns Instant.parse("2020-12-14T00:00:00Z")
        listOf(checkIn1, checkIn2).filterByAge(
            14,
            now
        ) shouldBe listOf(checkIn2)
    }

    @Test
    fun `filter works 2`() {
        val now = Instant.parse("2020-12-28T23:59:59Z")
        every { checkIn1.checkInEnd } returns Instant.parse("2020-12-12T00:00:00Z")
        every { checkIn2.checkInEnd } returns Instant.parse("2020-12-13T23:59:59Z")
        listOf(checkIn1, checkIn2).filterByAge(
            14,
            now
        ) shouldBe listOf()
    }

    @Test
    fun `filter works 3`() {
        val now = Instant.parse("2020-12-28T12:00:00Z")
        every { checkIn1.checkInEnd } returns Instant.parse("2020-12-13T11:59:59Z")
        every { checkIn2.checkInEnd } returns Instant.parse("2020-12-14T12:00:00Z")
        listOf(checkIn1, checkIn2).filterByAge(
            14,
            now
        ) shouldBe listOf(checkIn2)
    }

    @Test
    fun `filter works 4`() {
        val now = Instant.parse("2020-12-28T12:00:00Z")
        every { checkIn1.checkInEnd } returns Instant.parse("2020-12-14T00:00:00Z")
        every { checkIn2.checkInEnd } returns Instant.parse("2020-12-14T13:00:00Z")
        listOf(checkIn1, checkIn2).filterByAge(
            14,
            now
        ) shouldBe listOf(checkIn1, checkIn2)
    }

    @Test
    fun `filter works 5`() {
        val now = Instant.parse("2020-12-28T12:00:00Z")
        every { checkIn1.checkInEnd } returns "2020-12-13T13:59:59+02:00".toInstant()
        every { checkIn2.checkInEnd } returns "2020-12-14T15:00:00+02:00".toInstant()
        listOf(checkIn1, checkIn2).filterByAge(
            14,
            now
        ) shouldBe listOf(checkIn2)
    }
}
