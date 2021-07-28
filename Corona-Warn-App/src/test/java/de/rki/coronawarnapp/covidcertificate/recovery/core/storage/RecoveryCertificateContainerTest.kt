package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.recovery.RecoveryQrCodeTestData
import io.kotest.matchers.shouldNotBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class RecoveryCertificateContainerTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor
    private lateinit var extractorSpy: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        extractorSpy = spyk(extractor)
    }

    @Test
    fun `default parsing mode for containers is lenient`() {
        val container = RecoveryCertificateContainer(
            data = StoredRecoveryCertificateData(
                recoveryCertificateQrCode = RecoveryQrCodeTestData.validRecovery
            ),
            qrCodeExtractor = extractorSpy
        )

        container.certificateId shouldNotBe null

        verify {
            extractorSpy.extract(RecoveryQrCodeTestData.validRecovery, DccV1Parser.Mode.CERT_REC_LENIENT)
        }
    }
}
