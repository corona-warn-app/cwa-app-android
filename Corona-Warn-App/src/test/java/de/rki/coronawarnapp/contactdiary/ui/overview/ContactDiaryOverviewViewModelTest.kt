package de.rki.coronawarnapp.contactdiary.ui.overview

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.exporter.ContactDiaryExporter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskenf.RiskEnfItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderItem
import de.rki.coronawarnapp.contactdiary.util.ContactDiaryData
import de.rki.coronawarnapp.contactdiary.util.mockStringsForContactDiaryExporterTests
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.locationName
import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.extensions.observeForTesting
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(InstantExecutorExtension::class)
open class ContactDiaryOverviewViewModelTest {

    @MockK lateinit var taskController: TaskController
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var context: Context
    @MockK lateinit var checkInRepository: CheckInRepository

    private val testDispatcherProvider = TestDispatcherProvider()
    private val date = LocalDate.parse("2021-04-07")
    private val dateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @BeforeEach
    fun refresh() {
        MockKAnnotations.init(this)

        every { taskController.submit(any()) } just runs
        every { contactDiaryRepository.locationVisits } returns flowOf(emptyList())
        every { contactDiaryRepository.personEncounters } returns flowOf(emptyList())
        every { contactDiaryRepository.testResults } returns flowOf(emptyList())
        every { contactDiaryRepository.locations } returns flowOf(emptyList())
        every { contactDiaryRepository.submissions } returns flowOf(emptyList())
        every { contactDiaryRepository.people } returns flowOf(emptyList())
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(emptyList())
        every { riskLevelStorage.traceLocationCheckInRiskStates } returns flowOf(emptyList())
        every { checkInRepository.checkInsWithinRetention } returns flowOf(emptyList())

        mockStringsForContactDiaryExporterTests(context)
        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(dateMillis)
    }

    private val person = DefaultContactDiaryPerson(123, "Romeo")
    private val location = DefaultContactDiaryLocation(124, "Rewe")
    private val personEncounter = DefaultContactDiaryPersonEncounter(125, date, person)
    private val locationVisit = DefaultContactDiaryLocationVisit(126, date, location)

    private val checkInLow = mockk<CheckIn>().apply {
        every { id } returns 147
        every { traceLocationId } returns "12ab-34cd-56ef-78gh-456".decodeBase64()!!
        every { description } returns "Jahrestreffen der deutschen SAP Anwendergruppe"
        every { address } returns "Hauptstr 3, 69115 Heidelberg"
        every { traceLocationStart } returns null
        every { traceLocationEnd } returns null
    }

    private val checkInHigh = mockk<CheckIn>().apply {
        every { id } returns 148
        every { traceLocationId } returns "12ab-34cd-56ef-78gh-457".decodeBase64()!!
        every { description } returns "Kiosk"
        every { address } returns "Hauptstr 4, 69115 Heidelberg"
        every { traceLocationStart } returns null
        every { traceLocationEnd } returns null
    }

    private val locationEventLowRisk = DefaultContactDiaryLocation(
        locationId = 456,
        locationName = checkInLow.locationName,
        traceLocationID = checkInLow.traceLocationId
    )

    private val locationEventHighRisk = DefaultContactDiaryLocation(
        locationId = 457,
        locationName = checkInHigh.locationName,
        traceLocationID = checkInHigh.traceLocationId
    )

    private val locationEventLowRiskVisit = DefaultContactDiaryLocationVisit(
        id = 458,
        date = date,
        contactDiaryLocation = locationEventLowRisk,
        checkInID = 147L
    )

    private val locationEventHighRiskVisit = DefaultContactDiaryLocationVisit(
        id = 459,
        date = date,
        contactDiaryLocation = locationEventHighRisk,
        checkInID = 148L
    )

    private val traceLocationCheckInRiskLow = object : TraceLocationCheckInRisk {
        override val checkInId: Long = checkInLow.id
        override val localDateUtc: LocalDate = date
        override val riskState: RiskState = RiskState.LOW_RISK
    }

    private val traceLocationCheckInRiskHigh = object : TraceLocationCheckInRisk {
        override val checkInId: Long = checkInHigh.id
        override val localDateUtc: LocalDate = date
        override val riskState: RiskState = RiskState.INCREASED_RISK
    }

    private val aggregatedRiskPerDateResultLowRisk = ExposureWindowDayRisk(
        dateMillisSinceEpoch = dateMillis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
        minimumDistinctEncountersWithLowRisk = 1,
        minimumDistinctEncountersWithHighRisk = 0
    )

    private val aggregatedRiskPerDateResultHighRiskDueToHighRiskEncounter = ExposureWindowDayRisk(
        dateMillisSinceEpoch = dateMillis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 0,
        minimumDistinctEncountersWithHighRisk = 1
    )

    private val aggregatedRiskPerDateResultHighRiskDueToLowRiskEncounter = ExposureWindowDayRisk(
        dateMillisSinceEpoch = dateMillis,
        riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
        minimumDistinctEncountersWithLowRisk = 10,
        minimumDistinctEncountersWithHighRisk = 0
    )

    fun createInstance(): ContactDiaryOverviewViewModel = ContactDiaryOverviewViewModel(
        taskController = taskController,
        dispatcherProvider = testDispatcherProvider,
        contactDiaryRepository = contactDiaryRepository,
        riskLevelStorage = riskLevelStorage,
        timeStamper = timeStamper,
        checkInRepository = checkInRepository,
        exporter = ContactDiaryExporter(
            context,
            timeStamper,
            testDispatcherProvider
        )
    )

    @Test
    fun `submit clean task on init`() {
        createInstance()

        verify(exactly = 1) { taskController.submit(any()) }
    }

    @Test
    fun `first item is subheader`() {
        createInstance().listItems.getOrAwaitValue().first() is OverviewSubHeaderItem
    }

    @Test
    fun `overview list lists all days as expected`() {
        with(createInstance().listItems.getOrAwaitValue().filterIsInstance<DayOverviewItem>()) {
            size shouldBe ContactDiaryOverviewViewModel.DAY_COUNT

            var days = 0L
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
        val listItem = DayOverviewItem(
            date = date,
            contactItem = ContactItem(emptyList())
        ) {}

        with(createInstance()) {
            onItemPress(listItem)
            val navigationEvent = routeToScreen.getOrAwaitValue()
            navigationEvent should
                beInstanceOf(ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment::class)
            navigationEvent as ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment
            navigationEvent.localDateString shouldBe
                ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment(
                    listItem.date
                ).localDateString
        }
    }

    @Test
    fun `low risk enf with person and location`() {
        every { contactDiaryRepository.personEncounters } returns flowOf(listOf(personEncounter))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationVisit))
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(listOf(aggregatedRiskPerDateResultLowRisk))

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            contactItem!!.data.validate(
                hasPerson = true,
                hasLocation = true
            )

            riskEnfItem!!.validate(
                highRisk = false,
                dueToLowEncounters = false,
                hasPersonOrLocation = true
            )
        }
    }

    @Test
    fun `low risk enf without person or location`() {
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(listOf(aggregatedRiskPerDateResultLowRisk))

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            contactItem shouldBe null

            riskEnfItem!!.validate(
                highRisk = false,
                dueToLowEncounters = false,
                hasPersonOrLocation = false
            )
        }
    }

    @Test
    fun `high risk enf due to high risk encounter with person and location`() {
        every { contactDiaryRepository.personEncounters } returns flowOf(listOf(personEncounter))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationVisit))
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultHighRiskDueToHighRiskEncounter
            )
        )

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            contactItem!!.data.validate(
                hasPerson = true,
                hasLocation = true
            )

            riskEnfItem!!.validate(
                highRisk = true,
                dueToLowEncounters = false,
                hasPersonOrLocation = true
            )
        }
    }

    @Test
    fun `high risk enf due to high risk encounter without person or location`() {
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultHighRiskDueToHighRiskEncounter
            )
        )

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            contactItem shouldBe null

            riskEnfItem!!.validate(
                highRisk = true,
                dueToLowEncounters = false,
                hasPersonOrLocation = false
            )
        }
    }

    @Test
    fun `high risk enf due to low risk encounter with person and location`() {
        every { contactDiaryRepository.personEncounters } returns flowOf(listOf(personEncounter))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationVisit))
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultHighRiskDueToLowRiskEncounter
            )
        )

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            contactItem!!.data.validate(
                hasPerson = true,
                hasLocation = true
            )

            riskEnfItem!!.validate(
                highRisk = true,
                dueToLowEncounters = true,
                hasPersonOrLocation = true
            )
        }
    }

    @Test
    fun `high risk enf due to low risk encounter without person or location`() {
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultHighRiskDueToLowRiskEncounter
            )
        )

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            contactItem shouldBe null

            riskEnfItem!!.validate(
                highRisk = true,
                dueToLowEncounters = true,
                hasPersonOrLocation = false
            )
        }
    }

    @Test
    fun `risk enf and risk event are independently of each other`() {
        every { riskLevelStorage.ewDayRiskStates } returns flowOf(listOf(aggregatedRiskPerDateResultLowRisk))
        every { riskLevelStorage.traceLocationCheckInRiskStates } returns flowOf(listOf(traceLocationCheckInRiskHigh))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationEventHighRiskVisit))
        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkInHigh))

        var item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            riskEnfItem!!.validate(
                highRisk = false,
                dueToLowEncounters = false,
                hasPersonOrLocation = true
            )

            riskEventItem!!.validate(highRisk = true)
        }

        every { riskLevelStorage.ewDayRiskStates } returns flowOf(
            listOf(
                aggregatedRiskPerDateResultHighRiskDueToLowRiskEncounter
            )
        )
        every { riskLevelStorage.traceLocationCheckInRiskStates } returns flowOf(listOf(traceLocationCheckInRiskLow))
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationEventLowRiskVisit))
        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkInLow))

        item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            riskEnfItem!!.validate(
                highRisk = true,
                dueToLowEncounters = true,
                hasPersonOrLocation = true
            )

            riskEventItem!!.validate(highRisk = false)
        }
    }

    @Test
    fun `risk event item is null if no trace location check in risk exist`() {
        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        item.riskEventItem shouldBe null
    }

    @Test
    fun `low risk event by attending event with low risk`() {
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationEventLowRiskVisit))
        every { riskLevelStorage.traceLocationCheckInRiskStates } returns flowOf(listOf(traceLocationCheckInRiskLow))
        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkInLow))

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            riskEventItem!!.validate(highRisk = false)

            riskEventItem!!.events.first().run {
                name shouldBe locationEventLowRisk.locationName
                riskInfoAddition shouldBe null
                bulledPointColor shouldBe R.color.colorBulletPointLowRisk
                description shouldBe checkInLow.description
            }
        }
    }

    @Test
    fun `high risk event by attending event with high risk`() {
        every { contactDiaryRepository.locationVisits } returns flowOf(listOf(locationEventHighRiskVisit))
        every { riskLevelStorage.traceLocationCheckInRiskStates } returns flowOf(listOf(traceLocationCheckInRiskHigh))
        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkInHigh))

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            riskEventItem!!.validate(highRisk = true)

            riskEventItem!!.events.first().run {
                name shouldBe locationEventHighRisk.locationName
                riskInfoAddition shouldBe null
                bulledPointColor shouldBe R.color.colorBulletPointHighRisk
                description shouldBe checkInHigh.description
            }
        }
    }

    @Test
    fun `text for each event tells if it caused a high or low risk when multiple events contribute to event risk`() {
        every { contactDiaryRepository.locationVisits } returns flowOf(
            listOf(
                locationEventLowRiskVisit,
                locationEventHighRiskVisit
            )
        )
        every { riskLevelStorage.traceLocationCheckInRiskStates } returns flowOf(
            listOf(
                traceLocationCheckInRiskLow,
                traceLocationCheckInRiskHigh
            )
        )

        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkInHigh, checkInLow))

        val item = createInstance().listItems.getOrAwaitValue().first {
            it is DayOverviewItem && it.date == date
        } as DayOverviewItem

        with(item) {
            riskEventItem!!.validate(highRisk = true)

            with(riskEventItem!!.events) {
                find { it.name == locationEventLowRisk.locationName }!!.also {
                    it.riskInfoAddition shouldBe R.string.contact_diary_trace_location_risk_low
                    it.bulledPointColor shouldBe R.color.colorBulletPointLowRisk
                }

                find { it.name == locationEventHighRisk.locationName }!!.also {
                    it.riskInfoAddition shouldBe R.string.contact_diary_trace_location_risk_high
                    it.bulledPointColor shouldBe R.color.colorBulletPointHighRisk
                }
            }
        }
    }

    @Test
    fun `onExportPress() should post export`() {
        // In this test, now = January, 15
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-15T00:00:00.000Z")

        every { contactDiaryRepository.personEncounters } returns
            flowOf(ContactDiaryData.TWO_PERSONS_WITH_PHONE_NUMBERS_AND_EMAIL)
        every { contactDiaryRepository.locationVisits } answers
            { flowOf(ContactDiaryData.TWO_LOCATIONS_WITH_DURATION) }

        val vm = createInstance()

        vm.onExportPress()

        vm.exportLocationsAndPersons.observeForTesting {
            vm.exportLocationsAndPersons.value shouldBe """
                Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.

                02.01.2021 Constantin Frenzel; Tel. +49 987 654321; eMail constantin.frenzel@example.com
                02.01.2021 Barber; Dauer 01:45 h
                01.01.2021 Andrea Steinhauer; Tel. +49 123 456789; eMail andrea.steinhauer@example.com
                01.01.2021 Bakery; Dauer 00:15 h
                
            """.trimIndent()
        }
    }

    private fun List<ContactItem.Data>.validate(hasPerson: Boolean, hasLocation: Boolean) {
        var count = 0
        if (hasPerson) count++
        if (hasLocation) count++

        size shouldBe count
        forEach {
            when (it.type) {
                ContactItem.Type.PERSON -> {
                    it.drawableId shouldBe R.drawable.ic_contact_diary_person_item
                }
                ContactItem.Type.LOCATION -> {
                    it.drawableId shouldBe R.drawable.ic_contact_diary_location_item
                }
            }
        }
    }

    private fun RiskEnfItem.validate(
        highRisk: Boolean,
        dueToLowEncounters: Boolean,
        hasPersonOrLocation: Boolean
    ) {
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

    private fun RiskEventItem.validate(highRisk: Boolean) {
        body shouldBe R.string.contact_diary_trace_location_risk_body

        when (highRisk) {
            true -> {
                title shouldBe R.string.contact_diary_high_risk_title
                drawableId shouldBe R.drawable.ic_high_risk_alert
            }
            false -> {
                title shouldBe R.string.contact_diary_low_risk_title
                drawableId shouldBe R.drawable.ic_low_risk_alert
            }
        }
    }
}
