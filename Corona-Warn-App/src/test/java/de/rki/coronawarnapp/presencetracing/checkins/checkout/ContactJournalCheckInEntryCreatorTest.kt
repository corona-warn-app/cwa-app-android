package de.rki.coronawarnapp.presencetracing.checkins.checkout

import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.common.locationName
import de.rki.coronawarnapp.util.toJavaTime
import de.rki.coronawarnapp.util.toJoda
import de.rki.coronawarnapp.util.toJodaTime
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
import java.time.Duration
import java.time.Instant

class ContactJournalCheckInEntryCreatorTest : BaseTest() {

    @MockK lateinit var contactDiaryRepo: ContactDiaryRepository

    private val testCheckIn = CheckIn(
        id = 42L,
        traceLocationId = "traceLocationId1".decodeBase64()!!,
        version = 1,
        type = 1,
        description = "Restaurant",
        address = "Around the corner",
        traceLocationStart = Instant.parse("2021-03-04T22:00:00Z"),
        traceLocationEnd = Instant.parse("2021-03-04T23:00:00Z"),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T22:00:00Z"),
        checkInEnd = Instant.parse("2021-03-04T23:00:00Z"),
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
        date = testCheckIn.checkInStart.toLocalDateUtc().toJodaTime(),
        contactDiaryLocation = testLocation,
        checkInID = testCheckIn.id,
        duration = Duration.ofMinutes(60).toJoda()
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
                it.date.toJavaTime() shouldBe testCheckIn.checkInStart.toLocalDateUtc()
                it.duration!!.toStandardMinutes().minutes shouldBe 60
                it.contactDiaryLocation shouldBe testLocation
            }
        }
    }

    @Test
    fun `CheckIn to ContactDiaryLocationVisit duration mapping is correct`() {
        createInstance().apply {
            // Rounds duration to closest 15 minutes
            testCheckIn.copy(checkInEnd = Instant.parse("2021-03-04T23:07:29Z")).toLocationVisit(testLocation)
                .also {
                    it.duration!!.toStandardMinutes().minutes shouldBe 60
                }

            testCheckIn.copy(checkInEnd = Instant.parse("2021-03-04T23:05:00Z")).toLocationVisit(testLocation)
                .also {
                    it.duration!!.toStandardMinutes().minutes shouldBe 70
                }

            testCheckIn.copy(checkInEnd = Instant.parse("2021-03-04T23:07:30Z")).toLocationVisit(testLocation)
                .also {
                    it.duration!!.toStandardMinutes().minutes shouldBe 50
                }

            testCheckIn.copy(checkInEnd = Instant.parse("2021-03-04T22:52:30Z")).toLocationVisit(testLocation)
                .also {
                    it.duration!!.toStandardMinutes().minutes shouldBe 60
                }

            testCheckIn.copy(checkInEnd = Instant.parse("2021-03-04T22:52:29Z")).toLocationVisit(testLocation)
                .also {
                    it.duration!!.toStandardMinutes().minutes shouldBe 45
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
            checkInStart = Instant.parse("2021-03-04T22:00:00Z"),
            checkInEnd = Instant.parse("2021-03-05T02:00:00Z")
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
