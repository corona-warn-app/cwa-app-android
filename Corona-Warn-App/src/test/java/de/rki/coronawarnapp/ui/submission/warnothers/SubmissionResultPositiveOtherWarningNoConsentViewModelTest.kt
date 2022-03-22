package de.rki.coronawarnapp.ui.submission.warnothers

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.Screen
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionResultPositiveOtherWarningNoConsentViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var tekHistoryUpdater: TEKHistoryUpdater
    @MockK lateinit var tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var testType: CoronaTest.Type

    private val coronaTestFlow = MutableStateFlow(
        mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns true
        }
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { tekHistoryUpdaterFactory.create(any()) } returns tekHistoryUpdater
        every { tekHistoryUpdater.updateTEKHistoryOrRequestPermission() } just Runs

        every { interoperabilityRepository.countryList } returns emptyFlow()

        submissionRepository.apply {
            coEvery { giveConsentToSubmission(any()) } just Runs
            every { testForType(any()) } returns coronaTestFlow
        }

        every { enfClient.isTracingEnabled } returns flowOf(true)
        every { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, any()) } just Runs
    }

    private fun createViewModel() = SubmissionResultPositiveOtherWarningNoConsentViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        tekHistoryUpdaterFactory = tekHistoryUpdaterFactory,
        autoSubmission = autoSubmission,
        enfClient = enfClient,
        interoperabilityRepository = interoperabilityRepository,
        submissionRepository = submissionRepository,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        checkInRepository = checkInRepository,
        testType = testType
    )

    @Test
    fun `consent is stored and tek history updated`() {
        coronaTestFlow.value = mockk<PersonalCoronaTest>().apply {
            every { isAdvancedConsentGiven } returns false
        }

        val viewModel = createViewModel()

        viewModel.onConsentButtonClicked()

        coVerify { submissionRepository.giveConsentToSubmission(any()) }
        verify { tekHistoryUpdater.updateTEKHistoryOrRequestPermission() }
    }

    @Test
    fun `onResume() should call analyticsKeySubmissionCollector for PCR tests`() {
        testType = PCR
        createViewModel().onResume()
        verify(exactly = 1) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, PCR) }
        verify(exactly = 0) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, RAPID_ANTIGEN)
        }
    }

    @Test
    fun `onResume() should call analyticsKeySubmissionCollector for RAT tests`() {
        testType = RAPID_ANTIGEN
        createViewModel().onResume()
        verify(exactly = 0) { analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, PCR) }
        verify(exactly = 1) {
            analyticsKeySubmissionCollector.reportLastSubmissionFlowScreen(Screen.WARN_OTHERS, RAPID_ANTIGEN)
        }
    }
}
