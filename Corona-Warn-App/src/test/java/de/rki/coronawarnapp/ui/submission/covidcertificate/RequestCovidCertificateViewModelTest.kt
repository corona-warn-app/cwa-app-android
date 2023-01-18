package de.rki.coronawarnapp.ui.submission.covidcertificate

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExtendWith(InstantExecutorExtension::class)
internal class RequestCovidCertificateViewModelTest : BaseTest() {

    @MockK lateinit var testRegistrationStateProcessor: TestRegistrationStateProcessor
    @MockK lateinit var coronaTest: BaseCoronaTest

    private val date = LocalDate.parse(
        "01.01.1987",
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    )

    private val ratQRCode = CoronaTestQRCode.RapidAntigen(
        hash = "hash",
        dateOfBirth = date,
        createdAt = Instant.EPOCH,
        rawQrCode = "rawQrCode"
    )
    private val pcrQRCode = CoronaTestQRCode.PCR(qrCodeGUID = "GUID", rawQrCode = "rawQrCode")
    private val rapidPCRQrCode = CoronaTestQRCode.RapidPCR(
        hash = "hash",
        dateOfBirth = date,
        createdAt = Instant.EPOCH,
        rawQrCode = "rawQrCode"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testRegistrationStateProcessor.apply {
            coEvery { startTestRegistration(any(), any(), any()) } returns mockk()
            coEvery { state } returns flowOf(TestRegistrationStateProcessor.State.Idle)
        }

        every { coronaTest.identifier } returns "identifier"
    }

    private fun createInstance(
        coronaTestQRCode: CoronaTestQRCode = pcrQRCode,
        coronTestConsent: Boolean = true,
        allowTestReplacement: Boolean = false
    ) = RequestCovidCertificateViewModel(
        testRequest = coronaTestQRCode,
        coronaTestConsent = coronTestConsent,
        allowTestReplacement = allowTestReplacement,
        personName = "Lara",
        registrationStateProcessor = testRegistrationStateProcessor,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun birthDateChanged() {
        createInstance().apply {
            birthDateChanged(date)
            birthDate.getOrAwaitValue() shouldBe date
        }
    }

    @Test
    fun `PCR onAgreeGC removes and registers new test`() {
        createInstance(allowTestReplacement = true).apply {
            birthDateChanged(date)
            onAgreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = pcrQRCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = true
                )
            }
        }
    }

    @Test
    fun `PCR onAgreeGC registers new test and does not remove old Test`() {
        createInstance(allowTestReplacement = false).apply {
            birthDateChanged(date)
            onAgreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = pcrQRCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = false
                )
            }
        }
    }

    @Test
    fun `PCR onDisagreeGC removes and registers new test`() {
        createInstance(allowTestReplacement = true).apply {
            onDisagreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = pcrQRCode.copy(isDccConsentGiven = false),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = true
                )
            }
        }
    }

    @Test
    fun `PCR onDisagreeGC registers new test and does not remove old Test`() {
        createInstance(allowTestReplacement = false).apply {
            onDisagreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = pcrQRCode.copy(isDccConsentGiven = false),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = false
                )
            }
        }
    }

    @Test
    fun `RAT onAgreeGC removes and registers new test`() {
        createInstance(coronaTestQRCode = ratQRCode, allowTestReplacement = true).apply {
            onAgreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = ratQRCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = true
                )
            }
        }
    }

    @Test
    fun `RAT onAgreeGC registers new test and does not remove old Test`() {
        createInstance(coronaTestQRCode = ratQRCode, allowTestReplacement = false).apply {
            onAgreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = ratQRCode.copy(isDccConsentGiven = true),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = false
                )
            }
        }
    }

    @Test
    fun `RAT onDisagreeGC removes and registers new test`() {
        createInstance(coronaTestQRCode = ratQRCode, allowTestReplacement = true).apply {
            onDisagreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = ratQRCode.copy(isDccConsentGiven = false),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = true
                )
            }
        }
    }

    @Test
    fun `RAT onDisagreeGC registers new test and does not remove old Test`() {
        createInstance(coronaTestQRCode = ratQRCode, allowTestReplacement = false).apply {
            onDisagreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = ratQRCode.copy(isDccConsentGiven = false),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = false
                )
            }
        }
    }

    @Test
    fun `Rapid PCR onAgreeGC removes and registers new test`() {
        createInstance(coronaTestQRCode = rapidPCRQrCode, allowTestReplacement = true).apply {
            birthDateChanged(date)
            onAgreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = rapidPCRQrCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = true
                )
            }
        }
    }

    @Test
    fun `Rapid PCR onAgreeGC registers new test and does not remove old Test`() {
        createInstance(coronaTestQRCode = rapidPCRQrCode, allowTestReplacement = false).apply {
            birthDateChanged(date)
            onAgreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = rapidPCRQrCode.copy(isDccConsentGiven = true, dateOfBirth = date),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = false
                )
            }
        }
    }

    @Test
    fun `Rapid PCR onDisagreeGC removes and registers new test`() {
        createInstance(coronaTestQRCode = rapidPCRQrCode, allowTestReplacement = true).apply {
            onDisagreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = rapidPCRQrCode.copy(isDccConsentGiven = false),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = true
                )
            }
        }
    }

    @Test
    fun `Rapid PCR onDisagreeGC registers new test and does not remove old Test`() {
        createInstance(coronaTestQRCode = rapidPCRQrCode, allowTestReplacement = false).apply {
            onDisagreeGC()

            coVerify {
                testRegistrationStateProcessor.startTestRegistration(
                    request = rapidPCRQrCode.copy(isDccConsentGiven = false),
                    isSubmissionConsentGiven = any(),
                    allowTestReplacement = false
                )
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
}
