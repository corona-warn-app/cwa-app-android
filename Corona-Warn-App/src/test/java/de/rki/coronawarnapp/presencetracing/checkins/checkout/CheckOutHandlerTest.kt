package de.rki.coronawarnapp.presencetracing.checkins.checkout

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.temporal.ChronoUnit

class CheckOutHandlerTest : BaseTest() {

    @MockK lateinit var repository: CheckInRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var contactJournalCheckInEntryCreator: ContactJournalCheckInEntryCreator

    private val testCheckIn = CheckIn(
        id = 42L,
        traceLocationId = "traceLocationId1".encode(),
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH.plus(100, ChronoUnit.MILLIS),
        completed = false,
        createJournalEntry = true
    )

    private val testCheckInDontCreate = testCheckIn.copy(
        id = 43L,
        createJournalEntry = false
    )

    private var updatedCheckIn: CheckIn? = null
    private val nowUTC = Instant.ofEpochMilli(50)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUTC

        coEvery { repository.updateCheckIn(42, any()) } coAnswers {
            val callback: (CheckIn) -> CheckIn = arg(1)
            updatedCheckIn = callback(testCheckIn)
        }

        coEvery { repository.updateCheckIn(43, any()) } coAnswers {
            val callback: (CheckIn) -> CheckIn = arg(1)
            updatedCheckIn = callback(testCheckInDontCreate)
        }

        coEvery { contactJournalCheckInEntryCreator.createEntry(any()) } just runs
    }

    private fun createInstance() = CheckOutHandler(
        repository = repository,
        timeStamper = timeStamper,
        contactJournalCheckInEntryCreator = contactJournalCheckInEntryCreator
    )

    @Test
    fun `manual checkout`() = runTest {
        val instance = createInstance()
        instance.checkOut(42)
        updatedCheckIn shouldBe testCheckIn.copy(
            checkInEnd = nowUTC,
            completed = true
        )

        coVerify(exactly = 1) {
            contactJournalCheckInEntryCreator.createEntry(any())
        }

        // TODO cancel auto checkouts
    }

    @Test
    fun `Creates entry if create journal entry is true`() = runTest {
        createInstance().apply {
            checkOut(42)
        }

        updatedCheckIn!!.createJournalEntry shouldBe true

        coVerify(exactly = 1) {
            contactJournalCheckInEntryCreator.createEntry(any())
        }
    }

    @Test
    fun `Does not create entry if create journal entry is false`() = runTest {
        createInstance().apply {
            checkOut(43)
        }

        updatedCheckIn!!.createJournalEntry shouldBe false

        coVerify(exactly = 0) {
            contactJournalCheckInEntryCreator.createEntry(any())
        }
    }
}
