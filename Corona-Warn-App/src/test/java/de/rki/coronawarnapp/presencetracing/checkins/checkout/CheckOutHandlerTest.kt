package de.rki.coronawarnapp.presencetracing.checkins.checkout

import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CheckOutHandlerTest : BaseTest() {

    @MockK lateinit var repository: CheckInRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var diaryRepository: ContactDiaryRepository

    private val testCheckIn = CheckIn(
        id = 42L,
        guid = "eventOne",
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH.plus(100),
        completed = false,
        createJournalEntry = true
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
    }

    private fun createInstance() = CheckOutHandler(
        repository = repository,
        timeStamper = timeStamper,
        diaryRepository = diaryRepository,
    )

    @Test
    fun `manual checkout`() = runBlockingTest {
        val instance = createInstance()
        instance.checkOut(42)
        updatedCheckIn shouldBe testCheckIn.copy(
            checkInEnd = nowUTC,
            completed = true
        )
        // TODO journal creation
        // TODO cancel auto checkouts
    }
}
