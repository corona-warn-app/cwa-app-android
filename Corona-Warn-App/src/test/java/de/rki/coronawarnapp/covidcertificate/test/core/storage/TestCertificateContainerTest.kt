package de.rki.coronawarnapp.covidcertificate.test.core.storage

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateTestData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.joda.time.Instant
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
            certificateId shouldBe "URN:UVCI:V1:DE:7WR8CE12Y8O2AN4NK320TPNKB1"
            data.testCertificateQrCode shouldBe certificateTestData.personATest2CertQRCodeString
            data.certificateReceivedAt shouldBe Instant.parse("1970-01-02T10:17:36.789Z")
            toTestCertificate(null, mockk()) shouldNotBe null
        }
    }

    @Test
    fun `pending check and nullability`() {
        certificateTestData.personATest3CertNokeyContainer.apply {
            isCertificateRetrievalPending shouldBe true
            certificateId shouldBe null
            data.testCertificateQrCode shouldBe null
            data.certificateReceivedAt shouldBe null
            toTestCertificate(mockk(), mockk()) shouldBe null
        }

        certificateTestData.personATest4CertPendingContainer.apply {
            isCertificateRetrievalPending shouldBe true
            certificateId shouldBe null
            data.testCertificateQrCode shouldBe null
            data.certificateReceivedAt shouldBe null
            toTestCertificate(mockk(), mockk()) shouldBe null
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

        container.certificateId shouldNotBe null

        verify {
            extractorSpy.extract(certificateTestData.personATest1CertQRCodeString, DccV1Parser.Mode.CERT_TEST_LENIENT)
        }
    }
}
