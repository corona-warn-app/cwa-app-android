package de.rki.coronawarnapp.presencetracing.checkins.checkout

import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
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
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encode
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactJournalCheckInEntryCreatorTest : BaseTest() {

    @MockK lateinit var contactDiaryRepo: ContactDiaryRepository

    private val testCheckIn = CheckIn(
        id = 42L,
        traceLocationId = "traceLocationId1".decodeBase64()!!,
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = Instant.parse("2021-03-04T22:00+01:00"),
        traceLocationEnd = Instant.parse("2021-03-04T23:00+01:00"),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T22:00+01:00"),
        checkInEnd = Instant.parse("2021-03-04T23:00+01:00"),
        completed = false,
        createJournalEntry = true
    )

    private val testCheckInNoTraceLocationStartDate = testCheckIn.copy(traceLocationStart = null)
    private val testCheckInNoTraceLocationEndDate = testCheckIn.copy(traceLocationEnd = null)
    private val testCheckInNoTraceLocationStartAndEndDate =
        testCheckIn.copy(traceLocationStart = null, traceLocationEnd = null)

    private val testLocation = DefaultContactDiaryLocation(
        locationId = 123L,
        locationName = "${testCheckIn.description}, ${testCheckIn.address}, ${testCheckIn.traceLocationStart?.toPrettyDate()} - ${testCheckIn.traceLocationEnd?.toPrettyDate()}",
        traceLocationID = testCheckIn.traceLocationId
    )

    private fun Instant.toPrettyDate(): String = toUserTimeZone().toString(DateTimeFormat.shortDateTime())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { contactDiaryRepo.locations } returns flowOf(emptyList())

        every { contactDiaryRepo.locationVisits } returns flowOf(emptyList())

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
        createInstance().apply {
            testCheckIn.createLocationIfMissing()

            coVerify(exactly = 1) {
                contactDiaryRepo.addLocation(any())
            }

            // Location with trace location id already exists, so that location will be used
            testCheckIn.createLocationIfMissing()
            testCheckIn.createLocationIfMissing()
            testCheckIn.createLocationIfMissing()
            testCheckIn.createLocationIfMissing()

            coVerify(exactly = 1) {
                contactDiaryRepo.addLocation(any())
            }
        }
    }

    @Test
    fun `Location name concatenates description, address and if both are set trace location start and end date`() {
        createInstance().apply {
            testCheckIn.validateLocationName(testCheckIn.toLocationName())
            testCheckInNoTraceLocationStartDate.validateLocationName(testCheckInNoTraceLocationStartDate.toLocationName())
            testCheckInNoTraceLocationEndDate.validateLocationName(testCheckInNoTraceLocationEndDate.toLocationName())
            testCheckInNoTraceLocationStartAndEndDate.validateLocationName(testCheckInNoTraceLocationStartAndEndDate.toLocationName())
        }
    }

    private fun CheckIn.validateLocationName(nameToValidate: String) {
        nameToValidate shouldBe when (traceLocationStart != null && traceLocationEnd != null) {
            true -> "$description, $address, ${traceLocationStart?.toPrettyDate()} - ${traceLocationEnd?.toPrettyDate()}"
            else -> "$description, $address"
        }
    }
}
