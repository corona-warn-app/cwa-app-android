package de.rki.coronawarnapp.ui.submission.warnothers

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
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

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { tekHistoryUpdaterFactory.create(any()) } returns tekHistoryUpdater
        every { tekHistoryUpdater.updateTEKHistoryOrRequestPermission() } just Runs

        every { interoperabilityRepository.countryList } returns emptyFlow()
        every { submissionRepository.giveConsentToSubmission() } just Runs

        every { enfClient.isTracingEnabled } returns flowOf(true)
    }

    private fun createViewModel() = SubmissionResultPositiveOtherWarningNoConsentViewModel(
        dispatcherProvider = TestDispatcherProvider,
        tekHistoryUpdaterFactory = tekHistoryUpdaterFactory,
        autoSubmission = autoSubmission,
        enfClient = enfClient,
        interoperabilityRepository = interoperabilityRepository,
        submissionRepository = submissionRepository
    )

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `consent is stored and tek history updated`() {
        val consentMutable = MutableStateFlow(false)
        every { submissionRepository.hasGivenConsentToSubmission } returns consentMutable

        val viewModel = createViewModel()

        viewModel.onConsentButtonClicked()

        verify { submissionRepository.giveConsentToSubmission() }
        verify { tekHistoryUpdater.updateTEKHistoryOrRequestPermission() }
    }
}
