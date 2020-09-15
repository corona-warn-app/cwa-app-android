package de.rki.coronawarnapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.playbook.BackgroundNoise
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.SubmitDiagnosisKeysTransaction
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.CoroutinesTestRule
import testhelpers.beEventContent

class SubmissionViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var webRequestBuilder: WebRequestBuilder

    @MockK
    private lateinit var backgroundNoise: BackgroundNoise

    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"
    private val viewModel: SubmissionViewModel = SubmissionViewModel()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        every { LocalData.testGUID(any()) } just Runs
        every { LocalData.registrationToken() } returns registrationToken

        mockkObject(SubmitDiagnosisKeysTransaction)
        coEvery {
            SubmitDiagnosisKeysTransaction.start(
                registrationToken,
                any(),
                any(),
                any()
            )
        } just Runs


        mockkStatic("de.rki.coronawarnapp.exception.reporting.ExceptionReporterKt")
        every { any<Throwable>().report(any()) } just Runs

        mockkObject(WebRequestBuilder.Companion)
        every { WebRequestBuilder.getInstance() } returns webRequestBuilder

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise
    }

    @Test
    fun scanStatusValid() {
        // start
        viewModel.scanStatus.value should beEventContent(ScanStatus.STARTED)

        // valid guid
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        viewModel.validateAndStoreTestGUID("https://localhost/?$guid")
        viewModel.scanStatus.value should beEventContent(ScanStatus.SUCCESS)

        // invalid guid
        viewModel.validateAndStoreTestGUID("https://no-guid-here")
        viewModel.scanStatus.value should beEventContent(ScanStatus.INVALID)
    }

    @Test
    fun setVisitedCountriesWorking() {
        val countryList = listOf("DE", "EN")
        viewModel.setVisitedCountries(countryList)

        viewModel.visitedCountries.value shouldBe countryList
    }

    @Test
    fun setConsentToFederationWorking() {
        val consent = true
        viewModel.setConsentToFederation(consent)

        viewModel.consentToFederation.value shouldBe consent
    }

    @ExperimentalCoroutinesApi
    @Test
    fun submitDiagnosisKeysShouldFail() {
        viewModel.submitDiagnosisKeys(listOf())

        viewModel.submissionState.value shouldBe ApiRequestState.FAILED
    }

    @ExperimentalCoroutinesApi
    @Test
    fun submitDiagnosisKeysShouldNotFail() {
        viewModel.setConsentToFederation(true)
        viewModel.setVisitedCountries(listOf())

        viewModel.submitDiagnosisKeys(listOf())

        viewModel.submissionState.value shouldBe ApiRequestState.SUCCESS
    }
}
