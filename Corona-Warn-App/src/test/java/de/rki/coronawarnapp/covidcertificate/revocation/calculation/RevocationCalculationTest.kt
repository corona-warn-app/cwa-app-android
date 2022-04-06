package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates.Type
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import java.nio.file.Paths
import javax.inject.Inject

class RevocationCalculationTest: BaseTest() {

    @Inject lateinit var dccQrCodeExtractor: DccQrCodeExtractor
    @BaseGson @Inject lateinit var gson: Gson


    private val jsonFile = Paths.get(
        "src",
        "test",
        "resources",
        "revocation",
        "RevocationCalculationSampleData.json"
    ).toFile()

    private val instance = RevocationCalculation()

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `run test cases`() = runBlockingTest2 {
        val testCases: List<RevocationCalculationSampleData> = jsonFile.bufferedReader().use { gson.fromJson(it) }
        testCases.forEach {
            val dgc = createMockCert(it.barcodeData)

            val uci = instance.calculateRevocationEntryForType(dgc, Type.UCI)
            println("calculated uci=$uci, expUCI=${it.expUCI}")
            uci shouldBe it.expUCI

            val countryUci = instance.calculateRevocationEntryForType(dgc, Type.COUNTRYCODEUCI)
            println("calculated countryUci=$countryUci, expCOUNTRYCODEUCI=${it.expCOUNTRYCODEUCI}")
            countryUci shouldBe it.expCOUNTRYCODEUCI

            val signature = instance.calculateRevocationEntryForType(dgc, Type.SIGNATURE)
            println("calculated signature=$signature, expSIGNATURE=${it.expSIGNATURE}")
            signature shouldBe it.expSIGNATURE
        }
    }

    private suspend fun createMockCert(barcodeData: String): CwaCovidCertificate {
        val metadata = dccQrCodeExtractor.extract(barcodeData)
        metadata.data.certificate.payload.certificateIssuer
        return mockk {
            every { uniqueCertificateIdentifier } returns metadata.data.certificate.payload.uniqueCertificateIdentifier
            every { headerIssuer } returns metadata.data.header.issuer
        }
    }
}
