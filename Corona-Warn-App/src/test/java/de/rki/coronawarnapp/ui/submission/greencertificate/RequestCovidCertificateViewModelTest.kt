package de.rki.coronawarnapp.ui.submission.greencertificate

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
internal class RequestCovidCertificateViewModelTest : BaseTest() {

    @MockK lateinit var qrCodeRegistrationStateProcessor: QrCodeRegistrationStateProcessor
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var coronaTest: CoronaTest

    private val date = LocalDate.parse(
        "01.01.1987",
        DateTimeFormat.forPattern("dd.MM.yyyy")
    )

    private val ratQRCode = CoronaTestQRCode.RapidAntigen(hash = "hash", dateOfBirth = date, createdAt = Instant.EPOCH)
    private val pcrQRCode = CoronaTestQRCode.PCR("GUID")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        qrCodeRegistrationStateProcessor.apply {
            coEvery { startQrCodeRegistration(any(), any()) } just Runs
            coEvery { registrationError } returns SingleLiveEvent()
            coEvery { showRedeemedTokenWarning } returns SingleLiveEvent()
            coEvery { registrationState } returns MutableLiveData()
        }

        submissionRepository.apply {
            coEvery { registerTest(any()) } returns coronaTest
            coEvery { testForType(any()) } returns flowOf(coronaTest)
        }

        coEvery { coronaTestRepository.removeTest(any()) } returns coronaTest

        every { coronaTest.identifier } returns "identifier"
        every { analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any()) } just Runs
    }

    @Test
    fun birthDateChanged() {
        createInstance().apply {
            birthDateChanged(date)
            birthDate.getOrAwaitValue() shouldBe date
        }
    }

    @Test
    fun `PCR onAgreeGC removes and registers new test`() {
        createInstance(deleteOldTest = true).apply {
            birthDateChanged(date)
            onAgreeGC()

            coVerify {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    pcrQRCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    @Test
    fun `PCR onAgreeGC registers new test and does not remove old Test`() {
        createInstance(deleteOldTest = false).apply {
            birthDateChanged(date)
            onAgreeGC()

            coVerify {
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    pcrQRCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }

            coVerify(exactly = 0) {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
            }
        }
    }

    @Test
    fun `PCR onDisagreeGC removes and registers new test`() {
        createInstance(deleteOldTest = true).apply {
            onDisagreeGC()

            coVerify {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    pcrQRCode.copy(isDccConsentGiven = false),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    @Test
    fun `PCR onDisagreeGC registers new test and does not remove old Test`() {
        createInstance(deleteOldTest = false).apply {
            onDisagreeGC()

            coVerify {
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    pcrQRCode.copy(isDccConsentGiven = false),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }

            coVerify(exactly = 0) {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
            }
        }
    }

    @Test
    fun `RAT onAgreeGC removes and registers new test`() {
        createInstance(coronaTestQRCode = ratQRCode, deleteOldTest = true).apply {
            onAgreeGC()

            coVerify {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    ratQRCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    @Test
    fun `RAT onAgreeGC registers new test and does not remove old Test`() {
        createInstance(coronaTestQRCode = ratQRCode, deleteOldTest = false).apply {
            onAgreeGC()

            coVerify {
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    ratQRCode.copy(isDccConsentGiven = true),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }

            coVerify(exactly = 0) {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
            }
        }
    }

    @Test
    fun `RAT onDisagreeGC removes and registers new test`() {
        createInstance(coronaTestQRCode = ratQRCode, deleteOldTest = true).apply {
            onDisagreeGC()

            coVerify {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    ratQRCode.copy(isDccConsentGiven = false),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    @Test
    fun `RAT onDisagreeGC registers new test and does not remove old Test`() {
        createInstance(coronaTestQRCode = ratQRCode, deleteOldTest = false).apply {
            onDisagreeGC()

            coVerify {
                qrCodeRegistrationStateProcessor.startQrCodeRegistration(
                    ratQRCode.copy(isDccConsentGiven = false),
                    any()
                )
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }

            coVerify(exactly = 0) {
                submissionRepository.testForType(any())
                coronaTestRepository.removeTest(any())
            }
        }
    }

    @Test
    fun navigateBack() {
        createInstance().apply {
            navigateBack()
            events.getOrAwaitValue() shouldBe Back
        }
    }

    @Test
    fun navigateToHomeScreen() {
        createInstance().apply {
            navigateToHomeScreen()
            events.getOrAwaitValue() shouldBe ToHomeScreen
        }
    }

    @Test
    fun navigateToDispatcherScreen() {
        createInstance().apply {
            navigateToDispatcherScreen()
            events.getOrAwaitValue() shouldBe ToDispatcherScreen
        }
    }

    @Test
    fun `onAgreeGC reports analytics`() {
        createInstance(coronTestConsent = true).apply {
            onAgreeGC()
            coVerify {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    @Test
    fun `onDisagreeGC reports analytics`() {
        createInstance(coronTestConsent = true).apply {
            onDisagreeGC()
            coVerify {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    @Test
    fun `onAgreeGC does not report analytics`() {
        createInstance(coronTestConsent = false).apply {
            onAgreeGC()
            coVerify(exactly = 0) {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    @Test
    fun `onDisagreeGC does not report analytics`() {
        createInstance(coronTestConsent = false).apply {
            onDisagreeGC()
            coVerify(exactly = 0) {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any())
            }
        }
    }

    private fun createInstance(
        coronaTestQRCode: CoronaTestQRCode = pcrQRCode,
        coronTestConsent: Boolean = true,
        deleteOldTest: Boolean = false
    ) = RequestCovidCertificateViewModel(
        coronaTestQrCode = coronaTestQRCode,
        coronaTestConsent = coronTestConsent,
        deleteOldTest = deleteOldTest,
        coronaTestRepository = coronaTestRepository,
        submissionRepository = submissionRepository,
        qrCodeRegistrationStateProcessor = qrCodeRegistrationStateProcessor,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector
    )
}
