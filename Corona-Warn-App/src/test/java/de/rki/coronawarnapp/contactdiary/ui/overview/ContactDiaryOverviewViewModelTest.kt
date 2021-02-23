package de.rki.coronawarnapp.contactdiary.ui.overview

import android.content.Context
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.exporter.ContactDiaryExporter
import de.rki.coronawarnapp.contactdiary.util.ContactDiaryData
import de.rki.coronawarnapp.contactdiary.util.mockStringsForContactDiaryExporterTests
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.observeForTesting

@ExtendWith(MockKExtension::class, CoroutinesTestExtension::class, InstantExecutorExtension::class)
internal class ContactDiaryOverviewViewModelTest {

    @RelaxedMockK lateinit var taskController: TaskController
    @MockK lateinit var repository: ContactDiaryRepository
    @RelaxedMockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var context: Context

    private val testDispatcherProvider = TestDispatcherProvider()

    @BeforeEach
    fun setUp() {
        mockStringsForContactDiaryExporterTests(context)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance() = ContactDiaryOverviewViewModel(
        taskController,
        testDispatcherProvider,
        repository,
        riskLevelStorage,
        timeStamper,
        ContactDiaryExporter(
            context,
            timeStamper,
            testDispatcherProvider
        )
    )

    @Test
    fun `onExportPress() should post export`() {
        // In this test, now = January, 15
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-15T00:00:00.000Z")

        every { repository.personEncounters } returns flowOf(ContactDiaryData.TWO_PERSONS_WITH_PHONE_NUMBERS_AND_EMAIL)
        every { repository.locationVisits } answers { flowOf(ContactDiaryData.TWO_LOCATIONS_WITH_DURATION) }

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
}
