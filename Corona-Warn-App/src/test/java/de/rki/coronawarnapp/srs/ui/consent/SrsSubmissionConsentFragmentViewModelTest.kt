package de.rki.coronawarnapp.srs.ui.consent

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfigContainer
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.srs.ui.vm.TeksSharedViewModel
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
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
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var teksSharedViewModel: TeksSharedViewModel

    @MockK lateinit var checkIn: CheckIn

    fun createInstance(openTypeSelection: Boolean) = SrsSubmissionConsentFragmentViewModel(
        openTypeSelection,
        teksSharedViewModel,
        checkInRepository,
        appConfigProvider,
        dispatcherProvider = TestDispatcherProvider(),
        tekHistoryUpdaterFactory
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { tekHistoryUpdaterFactory.create(any()) } returns tekHistoryUpdater
        coEvery { appConfigProvider.currentConfig } returns flowOf(config())
        coEvery { teksSharedViewModel.setTekPatch(any()) } just Runs
    }

    @Test
    fun `when open type selection is true navigation to test type happens`() = runTest2 {
        val vm = createInstance(true)
        every { tekHistoryUpdater.getTeksOrRequestPermissionFromOS() } coAnswers {
            vm.onTekAvailable(listOf())
        }
        vm.submissionConsentAcceptButtonClicked()

        vm.event.value shouldBe SrsSubmissionConsentNavigationEvents.NavigateToTestType
    }

    @Test
    fun `when open type selection is false navigation to checkins happen when checkins exist`() = runTest2 {
        val vm = createInstance(false)
        every { checkIn.completed } returns true
        every { tekHistoryUpdater.getTeksOrRequestPermissionFromOS() } coAnswers {
            vm.onTekAvailable(listOf())
        }
        every { checkInRepository.checkInsWithinRetention } returns flowOf(listOf(checkIn))
        vm.submissionConsentAcceptButtonClicked()

        vm.event.value shouldBe SrsSubmissionConsentNavigationEvents.NavigateToShareCheckins
    }

    @Test
    fun `when open type selection is false navigation to symptoms happen when no checkin exists`() = runTest2 {
        val vm = createInstance(false)
        every { tekHistoryUpdater.getTeksOrRequestPermissionFromOS() } coAnswers {
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

    private fun config() = mockk<ConfigData>().apply {
        every { selfReportSubmission } returns SelfReportSubmissionConfigContainer.DEFAULT
    }
}
