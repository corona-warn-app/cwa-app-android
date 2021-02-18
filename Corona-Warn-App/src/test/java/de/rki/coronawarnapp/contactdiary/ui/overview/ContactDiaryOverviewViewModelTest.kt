package de.rki.coronawarnapp.contactdiary.ui.overview

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.util.ContactDiaryData
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
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

    @BeforeEach
    fun setUp() {
        val fromSlot = slot<String>()
        val toSlot = slot<String>()

        every {
            context.getString(
                R.string.contact_diary_export_intro_one,
                capture(fromSlot),
                capture(toSlot)
            )
        } answers { "Kontakte der letzten 15 Tage (${fromSlot.captured} - ${toSlot.captured})" }

        every {
            context.getString(R.string.contact_diary_export_intro_two)
        } answers { "Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG." }
    }

    @AfterEach
    fun tearDown() {
    }

    private fun createInstance() = ContactDiaryOverviewViewModel(
        taskController,
        TestDispatcherProvider(),
        repository,
        riskLevelStorage,
        timeStamper
    )

    @Test
    fun onExportPress() {

        // In this test, now = January, 15
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-15T00:00:00.000Z")

        every { repository.locationVisits } answers { flowOf(ContactDiaryData.TWO_LOCATIONS) }
        every { repository.personEncounters } returns flowOf(ContactDiaryData.TWO_PERSONS)

        val vm = createInstance()

        vm.onExportPress(context)

        vm.exportLocationsAndPersons.observeForTesting {
            vm.exportLocationsAndPersons.value shouldBe """
                Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.

                02.01.2021 Constantin Frenzel
                02.01.2021 Barber
                01.01.2021 Andrea Steinhauer
                01.01.2021 Bakery
                
                """
                .trimIndent()
        }
    }
}