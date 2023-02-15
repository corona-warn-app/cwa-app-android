package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeFragment
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeFragmentArgs
import de.rki.coronawarnapp.ui.submission.testresult.negative.SubmissionTestResultNegativeViewModel
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SubmissionTestResultNegativeFragmentTest : BaseUITest() {

    lateinit var viewModel: SubmissionTestResultNegativeViewModel
    @MockK lateinit var certificateRepository: TestCertificateRepository
    @MockK lateinit var recycledCoronaTestsProvider: RecycledCoronaTestsProvider
    @MockK lateinit var coronaTestProvider: CoronaTestProvider
    private val resultNegativeFragmentArgs = SubmissionTestResultNegativeFragmentArgs(testIdentifier = "").toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { coronaTestProvider.getTestForIdentifier(any()) } returns flowOf()
        every { certificateRepository.certificates } returns flowOf()

        viewModel = spyk(
            SubmissionTestResultNegativeViewModel(
                TestDispatcherProvider(),
                recycledTestProvider = recycledCoronaTestsProvider,
                certificateRepository = certificateRepository,
                testIdentifier = "",
                coronaTestProvider = coronaTestProvider
            )
        )
    }

    @Test
    @Screenshot
    fun capture_fragment_for_personal_test_pcr_negative() {
        every { viewModel.testResult } returns MutableLiveData(
            SubmissionTestResultNegativeViewModel.UIState(
                coronaTest = mockk<BaseCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.PCR_NEGATIVE
                    every { registeredAt } returns Instant.now()
                    every { type } returns BaseCoronaTest.Type.PCR
                    every { identifier } returns TestIdentifier()
                },
                certificateState = SubmissionTestResultNegativeViewModel.CertificateState.AVAILABLE
            )
        )

        every { viewModel.certificate } returns MutableLiveData(
            mockTestCertificate()
        )

        launchFragmentInContainer2<SubmissionTestResultNegativeFragment>(fragmentArgs = resultNegativeFragmentArgs)
        takeScreenshot<SubmissionTestResultNegativeFragment>("pcr_negative")
    }

    @Test
    @Screenshot
    fun capture_fragment_for_personal_test_rat_negative() {
        every { viewModel.testResult } returns MutableLiveData(
            SubmissionTestResultNegativeViewModel.UIState(
                coronaTest = mockk<BaseCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.RAT_NEGATIVE
                    every { registeredAt } returns Instant.now()
                    every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
                    every { identifier } returns TestIdentifier()
                },
                certificateState = SubmissionTestResultNegativeViewModel.CertificateState.NOT_REQUESTED
            )
        )

        every { viewModel.certificate } returns MutableLiveData(
            mockTestCertificate()
        )

        launchFragmentInContainer2<SubmissionTestResultNegativeFragment>(fragmentArgs = resultNegativeFragmentArgs)
        takeScreenshot<SubmissionTestResultNegativeFragment>("rat_negative")
    }

    @Test
    @Screenshot
    fun capture_fragment_for_family_test() {
        every { viewModel.testResult } returns MutableLiveData(
            SubmissionTestResultNegativeViewModel.UIState(
                coronaTest = mockk<FamilyCoronaTest>().apply {
                    every { testResult } returns CoronaTestResult.RAT_NEGATIVE
                    every { registeredAt } returns Instant.now()
                    every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
                    every { identifier } returns ""
                    every { personName } returns "Lara"
                    every { coronaTest } returns CoronaTest(
                        identifier = identifier,
                        type = type,
                        registeredAt = registeredAt,
                        registrationToken = "",
                        additionalInfo = CoronaTest.AdditionalInfo(Instant.now())
                    )
                },
                certificateState = SubmissionTestResultNegativeViewModel.CertificateState.AVAILABLE
            )
        )

        launchFragmentInContainer2<SubmissionTestResultNegativeFragment>(fragmentArgs = resultNegativeFragmentArgs)
        takeScreenshot<SubmissionTestResultNegativeFragment>("rat_negative")
    }

    private fun mockTestCertificate(): TestCertificate = mockk<TestCertificate>().apply {
        every { uniqueCertificateIdentifier } returns "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
        every { fullName } returns "Andrea Schneider"
        every { rawCertificate } returns mockk<TestDccV1>().apply {
            every { test } returns mockk<DccV1.TestCertificateData>().apply {
                every { testType } returns "PCR-Test"
                every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
            }
        }
        every { containerId } returns TestCertificateContainerId("testCertificateContainerId")
        every { testType } returns "PCR-Test"
        every { isPCRTestCertificate } returns true
        every { dateOfBirthFormatted } returns "1943-04-18"
        every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
        every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { personIdentifier } returns certificatePersonIdentifier
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.testCertificate)
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirthFormatted = "1943-04-18"
        )
        every { isDisplayValid } returns true
        every { sampleCollectedAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { state } returns CwaCovidCertificate.State.Valid(Instant.now().plusMillis(20))
    }

    private val certificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1981-03-20",
        firstNameStandardized = "firstNameStandardized",
        lastNameStandardized = "lastNameStandardized",
    )
}
