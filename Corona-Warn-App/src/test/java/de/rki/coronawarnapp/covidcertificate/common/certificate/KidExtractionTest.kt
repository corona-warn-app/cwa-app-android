package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class KidExtractionTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `extract kid from protected header`() {
        extractor.extract(
            VaccinationQrCodeTestData.protectedKid
        ).data.kid shouldBe "yLHLNvSl428="
    }

    @Test
    fun `extract kid from unprotected header`() {
        extractor.extract(
            VaccinationQrCodeTestData.unprotectedKid
        ).data.kid shouldBe "yLHLNvSl428="
    }
}
