package de.rki.coronawarnapp.contactdiary.ui.overview

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ListItem
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.task.TaskController
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
open class ContactDiaryOverviewViewModelTest {

    @MockK lateinit var taskController: TaskController
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    private val testDispatcherProvider = TestDispatcherProvider()
    private val dateMillis = date.toDateTimeAtStartOfDay(DateTimeZone.UTC).millis

    @BeforeEach
    fun refresh() {
        MockKAnnotations.init(this)

        every { taskController.submit(any()) } just runs
        every { contactDiaryRepository.locationVisits } returns flowOf(emptyList())
        every { contactDiaryRepository.personEncounters } returns flowOf(emptyList())
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(emptyList())
    }

    private val person = DefaultContactDiaryPerson(123, "Romeo")
    private val location = DefaultContactDiaryLocation(124, "Rewe")
    private val personEncounter = DefaultContactDiaryPersonEncounter(125, date, person)
    private val locationVisit = DefaultContactDiaryLocationVisit(126, date, location)

    private val aggregatedRiskPerDateResultLowRisk = AggregatedRiskPerDateResult(
        dateMillisSinceEpoch = dateMillis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
        minimumDistinctEncountersWithLowRisk = 1,
        minimumDistinctEncountersWithHighRisk = 0
    )

    private val aggregatedRiskPerDateResultLowRiskDueToHighRiskEncounter = AggregatedRiskPerDateResult(
        dateMillisSinceEpoch = dateMillis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 1
    )

    private val aggregatedRiskPerDateResultLowRiskDueToLowRiskEncounter = AggregatedRiskPerDateResult(
        dateMillisSinceEpoch = dateMillis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 10,
        minimumDistinctEncountersWithHighRisk = 0
    )

    fun createInstance(): ContactDiaryOverviewViewModel = ContactDiaryOverviewViewModel(
        taskController = taskController,
        dispatcherProvider = testDispatcherProvider,
        contactDiaryRepository = contactDiaryRepository,
        riskLevelStorage = riskLevelStorage
    )

    @Test
    fun `submit clean task on init`() {
        createInstance()

        verify(exactly = 1) { taskController.submit(any()) }
    }

    @Test
    fun `overview list lists all days as expected`() {
        with(createInstance().listItems.getOrAwaitValue()) {
            size shouldBe ContactDiaryOverviewViewModel.DAY_COUNT

            var days = 0
            forEach { it.date shouldBe date.minusDays(days++) }
        }
    }

    @Test
    fun `navigate back to main activity`() {
        with(createInstance()) {
            onBackButtonPress()
            routeToScreen.getOrAwaitValue() shouldBe ContactDiaryOverviewNavigationEvents.NavigateToMainActivity
        }
    }

    @Test
    fun `navigate to day fragment with correct day`() {
        val listItem = ListItem(date)

        with(createInstance()) {
            onItemPress(listItem)
            val navigationEvent = routeToScreen.getOrAwaitValue()
            navigationEvent should beInstanceOf(ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment::class)
            navigationEvent as ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment
            navigationEvent.localDateString shouldBe ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment(
                listItem.date
            ).localDateString
        }
    }

    @Test
    fun `low risk with person and location`() {
        every { contactDiaryRepository.personEncounters } returns flowOf(listOf(personEncounter))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationVisit))
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(listOf(aggregatedRiskPerDateResultLowRisk))

        with(createInstance().listItems.getOrAwaitValue().first { it.date == date }) {
            data.validate(
                hasPerson = true,
                hasLocation = true
            )

            risk!!.validate(
                highRisk = false,
                dueToLowEncounters = false,
                hasPersonOrLocation = true
            )
        }
    }

    @Test
    fun `low risk without person or location`() {
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(listOf(aggregatedRiskPerDateResultLowRisk))

        with(createInstance().listItems.getOrAwaitValue().first { it.date == date }) {
            data.validate(
                hasPerson = false,
                hasLocation = false
            )

            risk!!.validate(
                highRisk = false,
                dueToLowEncounters = false,
                hasPersonOrLocation = false
            )
        }
    }

    @Test
    fun `high risk due to high risk encounter with person and location`() {
        every { contactDiaryRepository.personEncounters } returns flowOf(listOf(personEncounter))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationVisit))
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultLowRiskDueToHighRiskEncounter
            )
        )

        with(createInstance().listItems.getOrAwaitValue().first { it.date == date }) {
            data.validate(
                hasPerson = true,
                hasLocation = true
            )

            risk!!.validate(
                highRisk = true,
                dueToLowEncounters = false,
                hasPersonOrLocation = true
            )
        }
    }

    @Test
    fun `high risk due to high risk encounter without person or location`() {
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultLowRiskDueToHighRiskEncounter
            )
        )

        with(createInstance().listItems.getOrAwaitValue().first { it.date == date }) {
            data.validate(
                hasPerson = false,
                hasLocation = false
            )

            risk!!.validate(
                highRisk = true,
                dueToLowEncounters = false,
                hasPersonOrLocation = false
            )
        }
    }

    @Test
    fun `high risk due to low risk encounter with person and location`() {
        every { contactDiaryRepository.personEncounters } returns flowOf(listOf(personEncounter))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationVisit))
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultLowRiskDueToLowRiskEncounter
            )
        )

        with(createInstance().listItems.getOrAwaitValue().first { it.date == date }) {
            data.validate(
                hasPerson = true,
                hasLocation = true
            )

            risk!!.validate(
                highRisk = true,
                dueToLowEncounters = true,
                hasPersonOrLocation = true
            )
        }
    }

    @Test
    fun `high risk due to low risk encounter without person or location`() {
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultLowRiskDueToLowRiskEncounter
            )
        )

        with(createInstance().listItems.getOrAwaitValue().first { it.date == date }) {
            data.validate(
                hasPerson = false,
                hasLocation = false
            )

            risk!!.validate(
                highRisk = true,
                dueToLowEncounters = true,
                hasPersonOrLocation = false
            )
        }
    }

    private fun List<ListItem.Data>.validate(hasPerson: Boolean, hasLocation: Boolean) {
        var count = 0
        if (hasPerson) count++
        if (hasLocation) count++

        size shouldBe count
        forEach {
            when (it.type) {
                ListItem.Type.PERSON -> {
                    it.drawableId shouldBe R.drawable.ic_contact_diary_person_item
                    it.text shouldBe person.fullName
                }
                ListItem.Type.LOCATION -> {
                    it.drawableId shouldBe R.drawable.ic_contact_diary_location_item
                    it.text shouldBe location.locationName
                }
            }
        }
    }

    private fun ListItem.Risk.validate(highRisk: Boolean, dueToLowEncounters: Boolean, hasPersonOrLocation: Boolean) {
        when (highRisk) {
            true -> {
                title shouldBe R.string.contact_diary_high_risk_title
                drawableId shouldBe R.drawable.ic_high_risk_alert
                body shouldBe when (dueToLowEncounters) {
                    true -> R.string.contact_diary_risk_body_high_risk_due_to_low_risk_encounters
                    false -> R.string.contact_diary_risk_body
                }
            }
            false -> {
                title shouldBe R.string.contact_diary_low_risk_title
                body shouldBe R.string.contact_diary_risk_body
                drawableId shouldBe R.drawable.ic_low_risk_alert
            }
        }

        bodyExtended shouldBe when (hasPersonOrLocation) {
            true -> R.string.contact_diary_risk_body_extended
            false -> null
        }
    }

    companion object {
        private val date = LocalDate.parse("2021-02-13")

        @JvmStatic
        @BeforeAll
        fun setup() {
            //mockkStatic(LocalDate::class)
            every { LocalDate.now() } returns date
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            clearStaticMockk(LocalDate::class)
        }
    }
}
