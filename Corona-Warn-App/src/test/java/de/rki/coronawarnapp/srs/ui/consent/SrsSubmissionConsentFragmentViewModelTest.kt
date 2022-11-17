package de.rki.coronawarnapp.srs.ui.consent

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
open class SrsSubmissionConsentFragmentViewModelTest {

    @MockK lateinit var tekHistoryUpdater: TEKHistoryUpdater
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory
    @MockK lateinit var checkInRepository: CheckInRepository

    @MockK lateinit var checkIn: CheckIn

    fun createInstance(openTypeSelection: Boolean) = SrsSubmissionConsentFragmentViewModel(
        openTypeSelection,
        checkInRepository,
        dispatcherProvider = TestDispatcherProvider(),
        tekHistoryUpdaterFactory
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { tekHistoryUpdaterFactory.create(any()) } returns tekHistoryUpdater
    }

    @Test
    fun `when open type selection is true navigation to test type happens`() = runTest2 {
        val vm = createInstance(true)
        every { tekHistoryUpdater.getTeksOrRequestPermission() } coAnswers {
            vm.onTekAvailable(listOf())
        }
        vm.submissionConsentAcceptButtonClicked()

        vm.event.value shouldBe SrsSubmissionConsentNavigationEvents.NavigateToTestType
    }

    @Test
    fun `when open type selection is false navigation to checkins happen when checkins exist`() = runTest2 {
        val vm = createInstance(false)
        every { checkIn.completed } returns true
        every { tekHistoryUpdater.getTeksOrRequestPermission() } coAnswers {
            vm.onTekAvailable(listOf())
        }
        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkIn))
        vm.submissionConsentAcceptButtonClicked()

        vm.event.value shouldBe SrsSubmissionConsentNavigationEvents.NavigateToShareCheckins
    }

    @Test
    fun `when open type selection is false navigation to symptoms happen when no checkin exists`() = runTest2 {
        val vm = createInstance(false)
        every { tekHistoryUpdater.getTeksOrRequestPermission() } coAnswers {
            vm.onTekAvailable(listOf())
        }
        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf())
        vm.submissionConsentAcceptButtonClicked()

        vm.event.value shouldBe SrsSubmissionConsentNavigationEvents.NavigateToShareSymptoms
    }

    @Test
    fun `vm navigates to privacy`() = runTest2 {
        val vm = createInstance(false)
        vm.onDataPrivacyClick()

        vm.event.value shouldBe SrsSubmissionConsentNavigationEvents.NavigateToDataPrivacy
    }

    @Test
    fun `vm navigates to main screen when flow is canceled`() = runTest2 {
        val vm = createInstance(false)
        vm.onConsentCancel()

        vm.event.value shouldBe SrsSubmissionConsentNavigationEvents.NavigateToMainScreen
    }
}
