package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor.Mode
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class VaccinationQrCodeValidatorTest : BaseTest() {
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
        verify { vacExtractorSpy.extract(testData.personAVac1QRCodeString, Mode.CERT_VAC_STRICT) }
    }
}
