package de.rki.coronawarnapp.srs.ui.typeselection

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
open class SrsTypeSelectionFragmentViewModelTest : BaseTest() {

    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var checkIn: CheckIn
    private val submissionType = SrsSubmissionType.SRS_OTHER

    fun createInstance() = SrsTypeSelectionFragmentViewModel(
        checkInRepository,
        dispatcherProvider = TestDispatcherProvider()
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { checkIn.completed } returns true
    }

    @Test
    fun `no navigation happens when no type is selected`() {
        val vm = createInstance()

        vm.onNextClicked()
        vm.navigation.value shouldBe null
    }

    @Test
    fun `cancel dialog is shown when cancelled`() {
        val vm = createInstance()

        vm.onCancel()
        vm.navigation.value shouldBe SrsTypeSelectionNavigationEvents.NavigateToCloseDialog
    }

    @Test
    fun `navigation to main screen happens when cancel dialog is confirmed`() {
        val vm = createInstance()

        vm.onCancelConfirmed()
        vm.navigation.value shouldBe SrsTypeSelectionNavigationEvents.NavigateToMainScreen
    }

    @Test
    fun `navigation to ShareCheckins when checkins exist`() {
        every { checkInRepository.completedCheckIns } returns flowOf(listOf(checkIn))
        val vm = createInstance()
        vm.selectTypeListItem(SrsTypeSelectionItem(true, submissionType))

        vm.onNextClicked()
        vm.navigation.value shouldBe SrsTypeSelectionNavigationEvents.NavigateToShareCheckins(submissionType)
    }

    @Test
    fun `navigation to ShareSymptoms when no checkins exist`() {
        every { checkInRepository.completedCheckIns } returns flowOf(listOf())
        val vm = createInstance()
        vm.selectTypeListItem(SrsTypeSelectionItem(true, submissionType))

        vm.onNextClicked()
        vm.navigation.value shouldBe SrsTypeSelectionNavigationEvents.NavigateToShareSymptoms(submissionType)
    }
}
