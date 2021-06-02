package de.rki.coronawarnapp.ui.submission.greencertificate

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
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

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        qrCodeRegistrationStateProcessor.apply {
            coEvery { startQrCodeRegistration(any(), any()) } just Runs
            coEvery { registrationError } returns SingleLiveEvent()
            coEvery { showRedeemedTokenWarning } returns SingleLiveEvent()
            coEvery { registrationState } returns MutableLiveData()
        }

        coEvery { submissionRepository.registerTest(any()) } returns mockk()
    }

    @Test
    fun birthDateChanged() {
        val date = LocalDate.parse(
            "01.01.1987",
            DateTimeFormat.forPattern("dd.MM.yyyy")
        )
        createInstance().apply {
            birthDateChanged(date)
            birthDate.getOrAwaitValue() shouldBe date
        }
    }

    @Test
    fun onAgreeGC() {
        createInstance().apply {
            onAgreeGC()
        }
    }

    @Test
    fun onDisagreeGC() {
    }

    @Test
    fun navigateBack() {
    }

    @Test
    fun navigateToHomeScreen() {
    }

    @Test
    fun navigateToDispatcherScreen() {
    }

    private fun createInstance(
        coronaTestQRCode: CoronaTestQRCode = CoronaTestQRCode.PCR("GUID"),
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
