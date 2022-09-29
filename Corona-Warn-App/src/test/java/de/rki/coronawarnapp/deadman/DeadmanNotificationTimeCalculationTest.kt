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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class DeadmanNotificationTimeCalculationTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var keyCacheRepository: KeyCacheRepository

    private val allCachedKeysFlow = MutableStateFlow(emptyList<CachedKey>())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-01T23:00:00.000Z")
        coEvery { keyCacheRepository.allCachedKeys() } returns allCachedKeysFlow
    }

    private fun createTimeCalculator() = DeadmanNotificationTimeCalculation(
        timeStamper = timeStamper,
        keyCacheRepository = keyCacheRepository,
        installTime = Instant.parse("2020-08-27T14:00:00.000Z")
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
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00.000Z")

        createTimeCalculator().calculateDelay(Instant.parse("2020-08-27T14:00:00.000Z")) shouldBe 720
    }

    @Test
    fun `negative time difference`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T14:00:00.000Z")

        createTimeCalculator().calculateDelay(Instant.parse("2020-08-27T14:00:00.000Z")) shouldBe -2160
    }

    @Test
    fun `success in future case`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00.000Z")

        createTimeCalculator().calculateDelay(Instant.parse("2020-08-27T15:00:00.000Z")) shouldBe 2220
    }

    @Test
    fun `12 hours delay`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("14:00:00"))
        )

        createTimeCalculator().getDelayInMinutes() shouldBe 720

        coVerify(exactly = 1) { keyCacheRepository.allCachedKeys() }
    }

    @Test
    fun `12 hours delay - only completed results count`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-28T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("14:00:00")),
            mockCachedKey(
                keyDay = LocalDate.parse("2020-08-27"),
                keyHour = LocalTime.parse("16:00:00"),
                isComplete = false
            )
        )

        createTimeCalculator().getDelayInMinutes() shouldBe 720

        coVerify(exactly = 1) { keyCacheRepository.allCachedKeys() }
    }

    @Test
    fun `negative delay`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("14:00:00")),
        )

        createTimeCalculator().getDelayInMinutes() shouldBe -2160
    }

    @Test
    fun `success in future delay`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("15:00:00")),
        )

        createTimeCalculator().getDelayInMinutes() shouldBe 2220
    }

    @Test
    fun `initial delay - no successful calculations yet`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-27T14:00:00.000Z")
        allCachedKeysFlow.value = emptyList()

        createTimeCalculator().getDelayInMinutes() shouldBe 2160
    }

    @Test
    fun `ensure correct order`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T14:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-26")),
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("14:00:00")),
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27")), // newest
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("23:00:00")),
        )

        createTimeCalculator().getDelayInMinutes() shouldBe -3000
    }

    @Test
    fun `ensure correct order 2`() = runTest {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-30T10:00:00.000Z")
        allCachedKeysFlow.value = listOf(
            mockCachedKey(keyDay = LocalDate.parse("2020-08-26")), // day package
            mockCachedKey(keyDay = LocalDate.parse("2020-08-26"), keyHour = LocalTime.parse("14:00:00")),
            mockCachedKey(keyDay = LocalDate.parse("2020-08-27"), keyHour = LocalTime.parse("23:00:00")), // newest
        )
        createTimeCalculator().getDelayInMinutes() shouldBe -1380
    }
}
