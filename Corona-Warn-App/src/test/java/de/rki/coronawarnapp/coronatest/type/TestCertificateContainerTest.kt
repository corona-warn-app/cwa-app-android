package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.CoronaTestTestData
import de.rki.coronawarnapp.coronatest.DaggerCoronaTestTestComponent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class TestCertificateContainerTest : BaseTest() {

    @Inject lateinit var testData: CoronaTestTestData

    @BeforeEach
    fun setup() {
        DaggerCoronaTestTestComponent.factory().create().inject(this)
    }

    @Test
    fun `ui facing test certificate creation and fallbacks`() {
        testData.personATest2CertContainer.apply {
            isPublicKeyRegistered shouldBe true
            isPending shouldBe false
            certificateId shouldBe "01DE/00001/1119305005/TODO"
            testCertificateQrCode shouldBe "personATest2CertQRCodeString"
            certificateReceivedAt shouldBe Instant.parse("1970-01-02T10:17:36.789Z")
            toTestCertificate(null) shouldNotBe null
        }
    }

    @Test
    fun `pending check and nullability`() {
        testData.personATest3CertContainerNokey.apply {
            isPublicKeyRegistered shouldBe false
            isPending shouldBe true
            certificateId shouldBe null
            testCertificateQrCode shouldBe null
            certificateReceivedAt shouldBe null
            toTestCertificate(mockk()) shouldBe null
        }

        testData.personATest4CertContainerPending.apply {
            isPublicKeyRegistered shouldBe true
            isPending shouldBe true
            certificateId shouldBe null
            testCertificateQrCode shouldBe null
            certificateReceivedAt shouldBe null
            toTestCertificate(mockk()) shouldBe null
        }
    }
}
