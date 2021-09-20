package de.rki.coronawarnapp.qrcode.scanner

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class QrCodeValidatorTest : BaseTest() {

    /**
     *   @BeforeEach
    fun setup() {
    DaggerCovidCertificateTestComponent.factory().create().inject(this)

    vacExtractorSpy = spyk(vacExtractor)
    }

    @Test
    fun `validator uses strict extraction mode`() = runBlockingTest {
    val instance = DccQrCodeValidator(vacExtractorSpy)
    instance.validate(testData.personAVac1QRCodeString).apply {
    uniqueCertificateIdentifier shouldBe testData.personAVac1Container.certificateId
    }
    verify { vacExtractorSpy.extract(testData.personAVac1QRCodeString, DccV1Parser.Mode.CERT_SINGLE_STRICT) }
    }

    @Test
    fun `validator throws invalid vaccination exception for pcr test qr code`() = runBlockingTest {
    val instance = DccQrCodeValidator(vacExtractorSpy)
    shouldThrow<InvalidHealthCertificateException> {
    instance.validate("HTTPS://LOCALHOST/?123456-12345678-1234-4DA7-B166-B86D85475064")
    }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_PREFIX_INVALID
    }
     */

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun setExtractors() {
    }

    @Test
    fun validate() {
    }
}
