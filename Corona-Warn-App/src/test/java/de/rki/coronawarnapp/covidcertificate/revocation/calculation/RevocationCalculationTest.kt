package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import javax.inject.Inject

class RevocationCalculationTest : BaseTest() {

    @Inject lateinit var dccQrCodeExtractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @ParameterizedTest
    @ArgumentsSource(RevocationCalculationTestCaseProvider::class)
    fun `run test cases`(testCase: RevocationCalculationTestCase) = runBlockingTest2 {
        val dgc = createMockCert(testCase.barcodeData)

        val uci = dgc.calculateRevocationEntryForType(RevocationHashType.UCI).hex()
        println("calculated uci=$uci, expUCI=${testCase.expUCI}")
        uci shouldBe testCase.expUCI

        val countryUci = dgc.calculateRevocationEntryForType(RevocationHashType.COUNTRYCODEUCI).hex()
        println("calculated countryUci=$countryUci, expCOUNTRYCODEUCI=${testCase.expCOUNTRYCODEUCI}")
        countryUci shouldBe testCase.expCOUNTRYCODEUCI

        val signature = dgc.calculateRevocationEntryForType(RevocationHashType.SIGNATURE).hex()
        println("calculated signature=$signature, expSIGNATURE=${testCase.expSIGNATURE}")
        signature shouldBe testCase.expSIGNATURE
    }

    private suspend fun createMockCert(barcodeData: String) = dccQrCodeExtractor.extract(barcodeData).data
}
