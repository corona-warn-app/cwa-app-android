package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.app.AlarmManager
import android.app.PendingIntent
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutHandler
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.temporal.ChronoUnit

class AutoCheckOutTest : BaseTest() {

    @MockK lateinit var intentFactory: AutoCheckOutIntentFactory
    @MockK lateinit var repository: CheckInRepository
    @MockK lateinit var checkOutHandler: CheckOutHandler
    @MockK lateinit var alarmManager: AlarmManager
    @MockK lateinit var timeStamper: TimeStamper

    private val baseCheckin = CheckIn(
        id = 0L,
        traceLocationId = "traceLocationId1".encode(),
        version = 1,
        type = 2,
        description = "brothers birthday",
        address = "Malibu",
        traceLocationStart = Instant.EPOCH,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH,
        completed = false,
        createJournalEntry = false
    )

    private val testCheckIn1 = baseCheckin.copy(
        id = 42L,
        checkInEnd = Instant.EPOCH.plus(500, ChronoUnit.MILLIS),
        completed = true
    )
    private val testCheckIn2 = baseCheckin.copy(
        id = 43L,
        checkInEnd = Instant.EPOCH.plus(600, ChronoUnit.MILLIS)
    )
    private val testCheckIn3 = baseCheckin.copy(
        id = 44L,
        checkInEnd = Instant.EPOCH.plus(700, ChronoUnit.MILLIS)
    )
    private val testCheckIn4 = baseCheckin.copy(
        id = 45L,
        checkInEnd = Instant.EPOCH.plus(2000, ChronoUnit.MILLIS)
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { repository.allCheckIns } returns flowOf(listOf(testCheckIn2, testCheckIn1, testCheckIn3, testCheckIn4))
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(1000, ChronoUnit.MILLIS)
        coEvery { repository.getCheckInById(42L) } returns testCheckIn1
        coEvery { repository.getCheckInById(43L) } returns testCheckIn2
        coEvery { repository.getCheckInById(44L) } returns testCheckIn3
        coEvery { repository.getCheckInById(45L) } returns testCheckIn4
        coEvery { checkOutHandler.checkOut(any(), any()) } just Runs

        every { intentFactory.createIntent(any()) } returns mockk()

        every { alarmManager.setExact(any(), any(), any()) } just Runs
        every { alarmManager.cancel(any<PendingIntent>()) } just Runs
    }

    fun createInstance(scope: CoroutineScope = TestScope()) = AutoCheckOut(
        appScope = scope,
        repository = repository,
        checkOutHandler = checkOutHandler,
        alarmManager = alarmManager,
        timeStamper = timeStamper,
        intentFactory = intentFactory,
    )

    @Test
    fun `process overdue`() = runTest {
        createInstance(scope = this).apply {
            processOverDueCheckouts() shouldBe listOf(43L, 44L)
        }

        coVerify(exactly = 0) {
            checkOutHandler.checkOut(42L, any())
            checkOutHandler.checkOut(45L, any())
        }
        coVerify {
            checkOutHandler.checkOut(43L, any())
            checkOutHandler.checkOut(44L, any())
        }
    }

    @Test
    fun `exceptions during processing of overdue checkins do not abort the whole process`() = runTest {
        coEvery { checkOutHandler.checkOut(43L, any()) } throws Exception()
        createInstance(scope = this).apply {
            processOverDueCheckouts()
        }

        coVerify(exactly = 0) {
            checkOutHandler.checkOut(42L, any())
            checkOutHandler.checkOut(45L, any())
        }
        coVerify {
            checkOutHandler.checkOut(43L, any())
            checkOutHandler.checkOut(44L, any())
        }
    }

    @Test
    fun `0id check-in is not processed`() = runTest {
        createInstance(scope = this).apply {
            performCheckOut(0L) shouldBe false
        }
        coVerify(exactly = 0) {
            repository.getCheckInById(0L)
        }
    }

    @Test
    fun `process check in`() = runTest {
        createInstance(scope = this).apply {
            performCheckOut(43L) shouldBe true
        }

        coVerifySequence {
            repository.getCheckInById(43L)
            checkOutHandler.checkOut(43L, any())
        }
    }

    @Test
    fun `null check ins are not processed`() = runTest {
        coEvery { repository.getCheckInById(42L) } returns null

        createInstance(scope = this).apply {
            performCheckOut(42L) shouldBe false
        }

        coVerifySequence {
            repository.getCheckInById(42L)
        }
    }

    @Test
    fun `exceptions during checkout are caught`() = runTest {
        coEvery { checkOutHandler.checkOut(42L, any()) } throws Exception()

        createInstance(scope = this).apply {
            performCheckOut(42L) shouldBe false
        }
    }

    @Test
    fun `alarm refresh targets the next check-out that is due`() = runTest {
        val mockIntent = mockk<PendingIntent>()
        every { intentFactory.createIntent(any()) } returns mockIntent

        createInstance(scope = this).apply {
            refreshAlarm() shouldBe true
        }

        verify {
            intentFactory.createIntent(45L)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, testCheckIn4.checkInEnd.toEpochMilli(), mockIntent)
        }
    }

    @Test
    fun `if there is no upcoming check-out, we cancel any alarms`() = runTest {
        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(2000, ChronoUnit.MILLIS)

        val mockIntent = mockk<PendingIntent>()
        every { intentFactory.createIntent(any()) } returns mockIntent

        createInstance(scope = this).apply {
            refreshAlarm() shouldBe false
        }

        verify {
            intentFactory.createIntent(null)
            alarmManager.cancel(mockIntent)
        }
    }
}
