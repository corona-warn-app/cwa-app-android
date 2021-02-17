package de.rki.coronawarnapp.contactdiary.retention

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactDiaryDataRetentionCalculationTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var contactDiaryRepository: DefaultContactDiaryRepository
    @MockK lateinit var riskLevelStorage: RiskLevelStorage

    private val testDates = arrayListOf<String>(
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

    @AfterEach
    fun teardown() {
        clearAllMocks()
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

        instance.getDaysDiff(LocalDate(Instant.parse("2020-08-20T14:00:00.000Z"))) shouldBe 0
        instance.getDaysDiff(LocalDate(Instant.parse("2020-08-20T13:00:00.000Z"))) shouldBe 0
        instance.getDaysDiff(LocalDate(Instant.parse("2020-08-19T14:00:00.000Z"))) shouldBe 1
        instance.getDaysDiff(LocalDate(Instant.parse("2020-08-05T14:00:00.000Z"))) shouldBe 15
        instance.getDaysDiff(LocalDate(Instant.parse("2020-08-04T14:00:00.000Z"))) shouldBe 16
        instance.getDaysDiff(LocalDate(Instant.parse("2020-08-03T14:00:00.000Z"))) shouldBe 17
    }

    @Test
    fun `filter by date`() {
        val localDate = DateTime.parse("2020-08-20T23:00:00.000Z").toLocalDate()

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
    fun `test location visit deletion`() = runBlockingTest {
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
        every { locationVisit.date } returns LocalDate(date)
        return locationVisit
    }

    @Test
    fun `test person encounters`() = runBlockingTest {
        val list: List<ContactDiaryPersonEncounter> = testDates.map { createContactDiaryPersonEncounter(Instant.parse(it)) }

        every { contactDiaryRepository.personEncounters } returns flowOf(list)
        coEvery { contactDiaryRepository.deletePersonEncounters(any()) } just runs

        val instance = createInstance()
        instance.filterContactDiaryPersonEncounters(list).size shouldBe 1
        instance.clearObsoleteContactDiaryPersonEncounters()
        coVerify(exactly = 1) { contactDiaryRepository.deletePersonEncounters(any()) }
    }

    private fun createContactDiaryPersonEncounter(date: Instant): ContactDiaryPersonEncounter {
        val personEncounter: ContactDiaryPersonEncounter = mockk()
        every { personEncounter.date } returns LocalDate(date)
        return personEncounter
    }

    @Test
    fun `test risk per date results`() = runBlockingTest {
        val instance = createInstance()
        val list: List<AggregatedRiskPerDateResult> = testDates.map { createAggregatedRiskPerDateResult(Instant.parse(it)) }
        val filteredList = list.filter { instance.isOutOfRetention(it.day) }

        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(list)
        coEvery { riskLevelStorage.deleteAggregatedRiskPerDateResults(any()) } just runs

        filteredList.size shouldBe 1
        instance.clearObsoleteRiskPerDate()
        coVerify { riskLevelStorage.deleteAggregatedRiskPerDateResults(filteredList) }
    }

    private fun createAggregatedRiskPerDateResult(date: Instant) = AggregatedRiskPerDateResult(
        dateMillisSinceEpoch = date.millis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 0
    )
}
