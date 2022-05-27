package de.rki.coronawarnapp.covidcertificate.test.core.storage

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateTestData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class TestCertificateContainerTest : BaseTest() {

    @Inject lateinit var certificateTestData: TestCertificateTestData
    @Inject lateinit var extractor: DccQrCodeExtractor
    private lateinit var extractorSpy: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        extractorSpy = spyk(extractor)
    }

    @Test
    fun `ui facing test certificate creation and fallbacks`() {
        certificateTestData.personATest2CertContainer.apply {
            isCertificateRetrievalPending shouldBe false
            qrCodeHash shouldBe
                certificateTestData.personATest2CertContainer.testCertificateQRCode?.qrCode?.toSHA256()
            data.testCertificateQrCode shouldBe certificateTestData.personATest2CertQRCodeString
            data.certificateReceivedAt shouldBe Instant.parse("1970-01-02T10:17:36.789Z")
            toTestCertificate(null, mockk()) shouldNotBe null
        }
    }

    @Test
    fun `pending check and nullability`() {
        certificateTestData.personATest3CertNokeyContainer.apply {
            isCertificateRetrievalPending shouldBe true
            qrCodeHash shouldBe data.identifier
            data.testCertificateQrCode shouldBe null
            data.certificateReceivedAt shouldBe null
            toTestCertificate(mockk(), mockk()) shouldBe null
        }

        certificateTestData.personATest4CertPendingContainer.apply {
            isCertificateRetrievalPending shouldBe true
            qrCodeHash shouldBe data.identifier
            data.testCertificateQrCode shouldBe null
            data.certificateReceivedAt shouldBe null
            toTestCertificate(mockk(), mockk()) shouldBe null
        }
    }

    @Test
    fun `check test certificate field mapping`() = runTest {
        val rawData = certificateTestData.personATest1CertQRCode().data
        certificateTestData.personATest1Container.toTestCertificate(
            certificateState = CwaCovidCertificate.State.Invalid()
        )!!.apply {
            headerIssuer shouldBe rawData.header.issuer
            certificateIssuer shouldBe rawData.certificate.test.certificateIssuer
        }
    }

    @Test
    fun `default parsing mode for containers is lenient`() {
        val container = TestCertificateContainer(
            data = PCRCertificateData(
                identifier = "",
                registrationToken = "",
                registeredAt = Instant.EPOCH,
                certificateReceivedAt = Instant.EPOCH,
                testCertificateQrCode = certificateTestData.personATest1CertQRCodeString
            ),
            qrCodeExtractor = extractorSpy
        )

        container.qrCodeHash shouldNotBe null
        container.personIdentifier shouldNotBe null

        coVerify {
            extractorSpy.extract(certificateTestData.personATest1CertQRCodeString, DccV1Parser.Mode.CERT_TEST_LENIENT)
        }
    }
}
