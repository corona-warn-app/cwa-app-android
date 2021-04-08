package de.rki.coronawarnapp.presencetracing.checkins.checkout

import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
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

class ContactJournalEntryCreatorTest : BaseTest() {

    @MockK lateinit var contactDiaryRepo: ContactDiaryRepository

    private val testCheckIn = CheckIn(
        id = 42L,
        traceLocationId = "traceLocationId1".encode(),
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = Instant.EPOCH,
        traceLocationEnd = Instant.EPOCH.plus(100),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH.plus(100),
        completed = false,
        createJournalEntry = true
    )

    private val textCheckInDontCreate = testCheckIn.copy(createJournalEntry = false)

    private val testLocation = DefaultContactDiaryLocation(
        locationId = 123L,
        locationName = "${testCheckIn.description}, ${testCheckIn.address}, ${testCheckIn.traceLocationStart} - ${testCheckIn.traceLocationEnd}",
        traceLocationID = testCheckIn.traceLocationId.utf8()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { contactDiaryRepo.locations } returns flowOf(emptyList())

        coEvery { contactDiaryRepo.addLocationVisit(any()) } just runs

        coEvery { contactDiaryRepo.addLocation(any()) } returns testLocation
    }

    private fun createInstance() = ContactJournalEntryCreator(
        diaryRepository = contactDiaryRepo
    )

    @Test
    fun `Creates entry if create journal entry is true`() = runBlockingTest {
        every { contactDiaryRepo.locations } returns flowOf(listOf(testLocation))
        createInstance().createEntry(testCheckIn)

        coVerify(exactly = 1) {
            contactDiaryRepo.locations
            contactDiaryRepo.addLocationVisit(any())
        }
    }

    @Test
    fun `Does not create entry if create journal entry is false`() = runBlockingTest {
        every { contactDiaryRepo.locations } returns flowOf(listOf(testLocation))
        createInstance().createEntry(textCheckInDontCreate)

        coVerify(exactly = 0) {
            contactDiaryRepo.locations
            contactDiaryRepo.addLocationVisit(any())
        }
    }

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
}
