package de.rki.coronawarnapp.ui.submission.qrcode.consent

import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.Country
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionConsentViewModelTest {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository

    lateinit var viewModel: SubmissionConsentViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { interoperabilityRepository.countryListFlow } returns MutableStateFlow(Country.values().toList())
        every { submissionRepository.giveConsentToSubmission() } just Runs
        viewModel =  SubmissionConsentViewModel(submissionRepository, interoperabilityRepository)
    }

    @Test
    fun testBackPressButton() {
        viewModel.onConsentButtonClick()
        verify(exactly = 1) { submissionRepository.giveConsentToSubmission() }
    }
}
