package de.rki.coronawarnapp.presencetracing.checkins.checkout

import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.encode
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactJournalCheckInEntryCreatorTest : BaseTest() {

    @MockK lateinit var contactDiaryRepo: ContactDiaryRepository

    private val testCheckIn = CheckIn(
        id = 42L,
        traceLocationId = "traceLocationId1".encode(),
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = Instant.parse("2021-03-04T21:00+01:00"),
        traceLocationEnd = Instant.parse("2021-03-04T23:00+01:00"),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T21:00+01:00"),
        checkInEnd = Instant.parse("2021-03-04T23:00+01:00"),
        completed = false,
        createJournalEntry = true
    )

    private val testLocation = DefaultContactDiaryLocation(
        locationId = 123L,
        locationName = "${testCheckIn.description}, ${testCheckIn.address}, ${testCheckIn.traceLocationStart} - ${testCheckIn.traceLocationEnd}",
        traceLocationID = testCheckIn.traceLocationId
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { contactDiaryRepo.locations } returns flowOf(emptyList())

        coEvery { contactDiaryRepo.addLocationVisit(any()) } just runs

        coEvery { contactDiaryRepo.addLocation(any()) } returns testLocation
    }

    private fun createInstance() = ContactJournalCheckInEntryCreator(
        diaryRepository = contactDiaryRepo
    )

    @Test
    fun `Creates location if missing`() = runBlockingTest {
        every { contactDiaryRepo.locations } returns flowOf(emptyList()) andThen flowOf(listOf(testLocation))

        // Repo returns an empty list for the first call, so location is missing and a new location should be created and added
        val instance = createInstance()
        instance.createEntry(testCheckIn)

        coVerify(exactly = 1) {
            contactDiaryRepo.addLocation(any())
        }

        // Location with trace location id already exists, so that location will be used
        instance.createEntry(testCheckIn)
        instance.createEntry(testCheckIn)
        instance.createEntry(testCheckIn)

        coVerify(exactly = 1) {
            contactDiaryRepo.addLocation(any())
        }
    }

    @Test
    fun `Location name contains trace location start and end date if both are set`() {
        createInstance().apply {
            testCheckIn.locationName() shouldBe "Restaurant, Around the corner, 04.03.21 21:00 - 04.03.21 23:00"
        }
    }
}
