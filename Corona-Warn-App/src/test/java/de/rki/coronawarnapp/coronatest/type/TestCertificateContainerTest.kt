package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.DaggerCoronaTestTestComponent
import de.rki.coronawarnapp.coronatest.TestCertificateTestData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class TestCertificateContainerTest : BaseTest() {

    @Inject lateinit var certificateTestData: TestCertificateTestData

    @BeforeEach
    fun setup() {
        DaggerCoronaTestTestComponent.factory().create().inject(this)
    }

    @Test
    fun `ui facing test certificate creation and fallbacks`() {
        certificateTestData.personATest2CertContainer.apply {
            isPublicKeyRegistered shouldBe true
            isCertificateRetrievalPending shouldBe false
            certificateId shouldBe "URN:UVCI:V1:DE:7WR8CE12Y8O2AN4NK320TPNKB1"
            data.testCertificateQrCode shouldBe certificateTestData.personATest2CertQRCodeString
            data.certificateReceivedAt shouldBe Instant.parse("1970-01-02T10:17:36.789Z")
            toTestCertificate(null) shouldNotBe null
        }
    }

    @Test
    fun `pending check and nullability`() {
        certificateTestData.personATest3CertNokeyContainer.apply {
            isPublicKeyRegistered shouldBe false
            isCertificateRetrievalPending shouldBe true
            certificateId shouldBe null
            data.testCertificateQrCode shouldBe null
            data.certificateReceivedAt shouldBe null
            toTestCertificate(mockk()) shouldBe null
        }

        certificateTestData.personATest4CertPendingContainer.apply {
            isPublicKeyRegistered shouldBe true
            isCertificateRetrievalPending shouldBe true
            certificateId shouldBe null
            data.testCertificateQrCode shouldBe null
            data.certificateReceivedAt shouldBe null
            toTestCertificate(mockk()) shouldBe null
        }
    }
}
