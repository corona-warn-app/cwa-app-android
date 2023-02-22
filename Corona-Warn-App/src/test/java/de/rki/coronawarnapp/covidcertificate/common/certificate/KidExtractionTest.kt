package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.di.DiTestProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class KidExtractionTest : BaseTest() {

    private val extractor: DccQrCodeExtractor = DiTestProvider.extractor

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
