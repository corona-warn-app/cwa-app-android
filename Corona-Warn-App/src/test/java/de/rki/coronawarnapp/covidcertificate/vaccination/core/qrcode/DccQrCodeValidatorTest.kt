package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccQrCodeValidatorTest : BaseTest() {
    @Inject lateinit var testData: VaccinationTestData
    @Inject lateinit var vacExtractor: DccQrCodeExtractor
    private lateinit var vacExtractorSpy: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerVaccinationTestComponent.factory().create().inject(this)

        vacExtractorSpy = spyk(vacExtractor)
    }

    @Test
    fun `validator uses strict extraction mode`() {
        val instance = DccQrCodeValidator(vacExtractorSpy)
        instance.validate(testData.personAVac1QRCodeString).apply {
            uniqueCertificateIdentifier shouldBe testData.personAVac1Container.certificateId
        }
        verify { vacExtractorSpy.extract(testData.personAVac1QRCodeString, DccV1Parser.Mode.CERT_SINGLE_STRICT) }
    }

    @Test
    fun `validator throws invalid vaccination exception for pcr test qr code`() {
        val instance = DccQrCodeValidator(vacExtractorSpy)
        shouldThrow<InvalidVaccinationCertificateException> {
            instance.validate("HTTPS://LOCALHOST/?123456-12345678-1234-4DA7-B166-B86D85475064")
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_PREFIX_INVALID
    }
}
