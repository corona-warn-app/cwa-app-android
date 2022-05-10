package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
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
    fun `extract kid from protected header`() = runTest {
        extractor.extract(
            VaccinationQrCodeTestData.protectedKid
        ).data.kid shouldBe "yLHLNvSl428="
    }

    @Test
    fun `extract kid from unprotected header`() = runTest {
        extractor.extract(
            VaccinationQrCodeTestData.unprotectedKid
        ).data.kid shouldBe "yLHLNvSl428="
    }

    @Test
    fun `extract kid from COSE without TAG`() = runTest {
        extractor.extract(
            VaccinationQrCodeTestData.coseWithoutTag
        ).data.kid shouldBe "f1sfUVIx8CA="
    }
}
