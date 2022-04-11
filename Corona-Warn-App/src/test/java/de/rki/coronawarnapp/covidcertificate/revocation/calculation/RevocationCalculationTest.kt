package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates.Type
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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

        val uci = dgc.calculateRevocationEntryForType(Type.UCI)
        println("calculated uci=$uci, expUCI=${testCase.expUCI}")
        uci shouldBe testCase.expUCI

        val countryUci = dgc.calculateRevocationEntryForType(Type.COUNTRYCODEUCI)
        println("calculated countryUci=$countryUci, expCOUNTRYCODEUCI=${testCase.expCOUNTRYCODEUCI}")
        countryUci shouldBe testCase.expCOUNTRYCODEUCI

        val signature = dgc.calculateRevocationEntryForType(Type.SIGNATURE)
        println("calculated signature=$signature, expSIGNATURE=${testCase.expSIGNATURE}")
        signature shouldBe testCase.expSIGNATURE
    }

    private suspend fun createMockCert(barcodeData: String): CwaCovidCertificate {
        val metadata = dccQrCodeExtractor.extract(barcodeData)
        return mockk {
            every { uniqueCertificateIdentifier } returns metadata.data.certificate.payload.uniqueCertificateIdentifier
            every { headerIssuer } returns metadata.data.header.issuer
            every { dccData.dscMessage } returns metadata.data.dscMessage
        }
    }
}
