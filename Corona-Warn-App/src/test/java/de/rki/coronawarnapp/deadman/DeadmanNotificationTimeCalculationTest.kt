package de.rki.coronawarnapp.deadman

import de.rki.coronawarnapp.diagnosiskeys.download.createMockCachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationTimeCalculationTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var keyCacheRepository: KeyCacheRepository

    private val allCachedKeysFlow = MutableStateFlow(emptyList<CachedKey>())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-01T23:00:00")
        coEvery { keyCacheRepository.allCachedKeys() } returns allCachedKeysFlow
    }

    private fun createTimeCalculator() = DeadmanNotificationTimeCalculation(
        timeStamper = timeStamper,
        keyCacheRepository = keyCacheRepository
    )

    private fun mockCachedKey(
        keyDay: LocalDate,
        keyHour: LocalTime? = null,
        isComplete: Boolean = true,
    ): CachedKey = mockk<CachedKey>().apply {
        every { info } returns createMockCachedKeyInfo(
            dayIdentifier = keyDay,
            hourIdentifier = keyHour,
            isComplete = isComplete,
        )
    }

    @Test
    fun `12 hours difference`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00")

        createTimeCalculator().getHoursDiff(Instant.parse("2020-08-27T14:00:00")) shouldBe 720
    }

    @Test
    fun `negative time difference`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T14:00:00")

        createTimeCalculator().getHoursDiff(Instant.parse("2020-08-27T14:00:00")) shouldBe -2160
    }

    @Test
    fun `success in future case`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00")

        createTimeCalculator().getHoursDiff(Instant.parse("2020-08-27T15:00:00")) shouldBe 2220
    }

    @Test
    fun `12 hours delay`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("14:00:00"))
        )

        createTimeCalculator().getDelay() shouldBe 720

        coVerify(exactly = 1) { keyCacheRepository.allCachedKeys() }
    }

    @Test
    fun `12 hours delay - only completed results count`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("14:00:00")),
            mockCachedKey(
                keyDay = LocalDate.parse("2020-08-27"),
                keyHour = LocalTime.parse("16:00:00"),
                isComplete = false
            )
        )

        createTimeCalculator().getDelay() shouldBe 720

        coVerify(exactly = 1) { keyCacheRepository.allCachedKeys() }
    }

    @Test
    fun `negative delay`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("14:00:00")),
        )

        createTimeCalculator().getDelay() shouldBe -2160
    }

    @Test
    fun `success in future delay`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("15:00:00")),
        )

        createTimeCalculator().getDelay() shouldBe 2220
    }

    @Test
    fun `initial delay - no successful calculations yet`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00")
        allCachedKeysFlow.value = emptyList()

        createTimeCalculator().getDelay() shouldBe 2160
    }
}
