package de.rki.coronawarnapp.contactdiary.retention

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiarySubmissionEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUserTz
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.random.Random

class ContactDiaryDataRetentionCalculationTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var contactDiaryRepository: DefaultContactDiaryRepository
    @MockK lateinit var riskLevelStorage: RiskLevelStorage

    private val testDates = arrayListOf(
        "2020-08-20T14:00:00.000Z",
        "2020-08-20T13:00:00.000Z",
        "2020-08-19T14:00:00.000Z",
        "2020-08-05T14:00:00.000Z",
        "2020-08-04T14:00:00.000Z",
        "2020-08-03T14:00:00.000Z"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-20T23:00:00.000Z")
    }

    private fun createInstance() = ContactDiaryRetentionCalculation(
        timeStamper = timeStamper,
        repository = contactDiaryRepository,
        riskLevelStorage = riskLevelStorage
    )

    @Test
    fun `test days diff`() {
        every { timeStamper.nowUTC } returns Instant.parse("2020-08-20T14:00:00.000Z")

        val instance = createInstance()

        instance.getDaysDiff(Instant.parse("2020-08-20T14:00:00.000Z").toLocalDateUserTz()) shouldBe 0
        instance.getDaysDiff(Instant.parse("2020-08-20T13:00:00.000Z").toLocalDateUserTz()) shouldBe 0
        instance.getDaysDiff(Instant.parse("2020-08-19T14:00:00.000Z").toLocalDateUserTz()) shouldBe 1
        instance.getDaysDiff(Instant.parse("2020-08-05T14:00:00.000Z").toLocalDateUserTz()) shouldBe 15
        instance.getDaysDiff(Instant.parse("2020-08-04T14:00:00.000Z").toLocalDateUserTz()) shouldBe 16
        instance.getDaysDiff(Instant.parse("2020-08-03T14:00:00.000Z").toLocalDateUserTz()) shouldBe 17
    }

    @Test
    fun `filter by date`() {
        val localDate = ZonedDateTime.parse("2020-08-20T23:00:00.000Z").toLocalDate()

        val instance = createInstance()

        instance.isOutOfRetention(localDate) shouldBe false
        instance.isOutOfRetention(localDate.minusDays(5)) shouldBe false
        instance.isOutOfRetention(localDate.minusDays(10)) shouldBe false
        instance.isOutOfRetention(localDate.minusDays(15)) shouldBe false
        instance.isOutOfRetention(localDate.minusDays(16)) shouldBe false
        instance.isOutOfRetention(localDate.minusDays(17)) shouldBe true
        instance.isOutOfRetention(localDate.minusDays(20)) shouldBe true
        instance.isOutOfRetention(localDate.minusDays(25)) shouldBe true
    }

    @Test
    fun `test location visit deletion`() = runTest {
        val list: List<ContactDiaryLocationVisit> = testDates.map { createContactDiaryLocationVisit(Instant.parse(it)) }

        every { contactDiaryRepository.locationVisits } returns flowOf(list)
        coEvery { contactDiaryRepository.deleteLocationVisits(any()) } just runs

        val instance = createInstance()
        instance.filterContactDiaryLocationVisits(list).size shouldBe 1

        instance.clearObsoleteContactDiaryLocationVisits()
        coVerify(exactly = 1) { contactDiaryRepository.deleteLocationVisits(any()) }
    }

    private fun createContactDiaryLocationVisit(date: Instant): ContactDiaryLocationVisit {
        val locationVisit: ContactDiaryLocationVisit = mockk()
        every { locationVisit.date } returns date.toLocalDateUserTz()
        return locationVisit
    }

    @Test
    fun `test person encounters`() = runTest {
        val list: List<ContactDiaryPersonEncounter> =
            testDates.map { createContactDiaryPersonEncounter(Instant.parse(it)) }

        every { contactDiaryRepository.personEncounters } returns flowOf(list)
        coEvery { contactDiaryRepository.deletePersonEncounters(any()) } just runs

        val instance = createInstance()
        instance.filterContactDiaryPersonEncounters(list).size shouldBe 1
        instance.clearObsoleteContactDiaryPersonEncounters()
        coVerify(exactly = 1) { contactDiaryRepository.deletePersonEncounters(any()) }
    }

    private fun createContactDiaryPersonEncounter(date: Instant): ContactDiaryPersonEncounter {
        val personEncounter: ContactDiaryPersonEncounter = mockk()
        every { personEncounter.date } returns date.toLocalDateUserTz()
        return personEncounter
    }

    @Test
    fun `test risk per date results`() = runTest {
        val instance = createInstance()
        val list: List<ExposureWindowDayRisk> = testDates.map { createAggregatedRiskPerDateResult(Instant.parse(it)) }
        val filteredList = list.filter { instance.isOutOfRetention(it.localDateUtc) }

        every { riskLevelStorage.ewDayRiskStates } returns flowOf(list)
        coEvery { riskLevelStorage.deleteAggregatedRiskPerDateResults(any()) } just runs

        filteredList.size shouldBe 1
        instance.clearObsoleteRiskPerDate()
        coVerify { riskLevelStorage.deleteAggregatedRiskPerDateResults(filteredList) }
    }

    private fun createAggregatedRiskPerDateResult(date: Instant) = ExposureWindowDayRisk(
        dateMillisSinceEpoch = date.toEpochMilli(),
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 0
    )

    @Test
    fun `test corona tests`() = runTest {
        createInstance().run {
            val list: List<ContactDiaryCoronaTestEntity> =
                testDates.map { createContactDiaryCoronaTestEntity(Instant.parse(it)) }
            val filteredList = list.filter { isOutOfRetention(it.time.toLocalDateUtc()) }

            every { contactDiaryRepository.testResults } returns flowOf(list)
            coEvery { contactDiaryRepository.deleteTests(any()) } just runs

            filteredList.size shouldBe 1
            clearObsoleteCoronaTests()
            coVerify { contactDiaryRepository.deleteTests(filteredList) }
        }
    }

    @Test
    fun `test submissions`() = runTest {
        createInstance().run {
            val list: List<ContactDiarySubmissionEntity> =
                testDates.map {
                    ContactDiarySubmissionEntity(
                        id = Random.nextLong(),
                        submittedAt = Instant.parse(it)
                    )
                }
            val filteredList = list.filter { isOutOfRetention(it.submittedAt.toLocalDateUtc()) }

            every { contactDiaryRepository.submissions } returns flowOf(list)
            coEvery { contactDiaryRepository.deleteSubmissions(any()) } just runs

            filteredList.size shouldBe 1
            clearObsoleteSubmissions()
            coVerify { contactDiaryRepository.deleteSubmissions(filteredList) }
        }
    }

    private fun createContactDiaryCoronaTestEntity(date: Instant) = ContactDiaryCoronaTestEntity(
        id = "Test for testing...",
        testType = ContactDiaryCoronaTestEntity.TestType.ANTIGEN,
        result = ContactDiaryCoronaTestEntity.TestResult.POSITIVE,
        time = date
    )
}
