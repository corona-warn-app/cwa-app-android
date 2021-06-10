package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor.Mode
import de.rki.coronawarnapp.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class VaccinationQrCodeValidatorTest : BaseTest() {
    @Inject lateinit var testData: VaccinationTestData
    @Inject lateinit var vacExtractor: VaccinationQRCodeExtractor
    private lateinit var vacExtractorSpy: VaccinationQRCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerVaccinationTestComponent.factory().create().inject(this)

        vacExtractorSpy = spyk(vacExtractor)
    }

    @Test
    fun `validator uses strict extraction mode`() {
        val instance = VaccinationQRCodeValidator(vacExtractorSpy)
        instance.validate(testData.personAVac1QRCodeString).apply {
            uniqueCertificateIdentifier shouldBe testData.personAVac1Container.certificateId
        }
        verify { vacExtractorSpy.extract(testData.personAVac1QRCodeString, Mode.CERT_VAC_STRICT) }
    }
}
