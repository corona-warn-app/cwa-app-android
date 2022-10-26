package de.rki.coronawarnapp.presencetracing.checkins.checkout

import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.common.locationName
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toInstant
import java.time.Duration

class ContactJournalCheckInEntryCreatorTest : BaseTest() {

    @MockK lateinit var contactDiaryRepo: ContactDiaryRepository

    private val testCheckIn = CheckIn(
        id = 42L,
        traceLocationId = "traceLocationId1".decodeBase64()!!,
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = "2021-03-04T22:00+01:00".toInstant(),
        traceLocationEnd = "2021-03-04T23:00+01:00".toInstant(),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = "2021-03-04T22:00+01:00".toInstant(),
        checkInEnd = "2021-03-04T23:00+01:00".toInstant(),
        completed = false,
        createJournalEntry = true
    )

    private val testLocation = DefaultContactDiaryLocation(
        locationId = 123L,
        locationName = testCheckIn.locationName,
        traceLocationID = testCheckIn.traceLocationId
    )

    private val testLocationVisit = DefaultContactDiaryLocationVisit(
        id = 0,
        date = testCheckIn.checkInStart.toLocalDateUtc(),
        contactDiaryLocation = testLocation,
        checkInID = testCheckIn.id,
        duration = Duration.ofMinutes(60)
    )

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
    fun `Creates location if missing`() = runTest {
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

            testCheckIn.copy(traceLocationId = "traceLocationId2".decodeBase64()!!).createLocationIfMissing()

            coVerify(exactly = 2) {
                contactDiaryRepo.addLocation(any())
            }
        }
    }

    @Test
    fun `CheckIn to ContactDiaryLocationVisit is correct`() {
        createInstance().apply {
            testCheckIn.toLocationVisit(testLocation).also {
                it.checkInID shouldBe testCheckIn.id
                it.date shouldBe testCheckIn.checkInStart.toLocalDateUtc()
                it.duration shouldBe Duration.ofMinutes(60)
                it.contactDiaryLocation shouldBe testLocation
            }
        }
    }

    @Test
    fun `CheckIn to ContactDiaryLocationVisit duration mapping is correct`() {
        createInstance().apply {
            // Rounds duration to closest 15 minutes
            testCheckIn.copy(checkInEnd = "2021-03-04T23:07:29+01:00".toInstant()).toLocationVisit(testLocation)
                .also {
                    it.duration shouldBe Duration.ofMinutes(60)
                }

            testCheckIn.copy(checkInEnd = "2021-03-04T23:07:30+01:00".toInstant()).toLocationVisit(testLocation)
                .also {
                    it.duration shouldBe Duration.ofMinutes(75)
                }

            testCheckIn.copy(checkInEnd = "2021-03-04T22:52:30+01:00".toInstant()).toLocationVisit(testLocation)
                .also {
                    it.duration shouldBe Duration.ofMinutes(60)
                }

            testCheckIn.copy(checkInEnd = "2021-03-04T22:52:29+01:00".toInstant()).toLocationVisit(testLocation)
                .also {
                    it.duration shouldBe Duration.ofMinutes(45)
                }
        }
    }

    @Test
    fun `Creates location visits if missing`() = runTest {
        every { contactDiaryRepo.locationVisits } returns flowOf(emptyList()) andThen flowOf(listOf(testLocationVisit))

        createInstance().apply {
            val checkins = mutableListOf(testCheckIn)

            checkins.createMissingLocationVisits(testLocation).also {
                it[0] shouldBe testLocationVisit
            }

            checkins.createMissingLocationVisits(testLocation).also {
                it.isEmpty() shouldBe true
            }

            // Create check in for next day which should also create a visit for the next day
            val testCheckInNextDay = testCheckIn.copy(
                checkInStart = testCheckIn.checkInStart.plus(Duration.ofDays(1)),
                checkInEnd = testCheckIn.checkInEnd.plus(Duration.ofDays(1))
            )
            checkins.add(testCheckInNextDay)

            checkins.createMissingLocationVisits(testLocation).also {
                it.size shouldBe 1 // and not 2
                it[0] shouldBe testLocationVisit.copy(date = testLocationVisit.date.plusDays(1))
            }
        }
    }

    @Test
    fun `Creates 1 location and 2 visits for split check in`() = runTest {
        val splitCheckIn = testCheckIn.copy(
            checkInStart = "2021-03-04T22:00+01:00".toInstant(),
            checkInEnd = "2021-03-05T02:00+01:00".toInstant()
        )
        createInstance().apply {
            createEntry(splitCheckIn)

            coVerify(exactly = 1) {
                contactDiaryRepo.addLocation(any())
            }

            coVerify(exactly = 2) {
                contactDiaryRepo.addLocationVisit(any())
            }
        }
    }
}
