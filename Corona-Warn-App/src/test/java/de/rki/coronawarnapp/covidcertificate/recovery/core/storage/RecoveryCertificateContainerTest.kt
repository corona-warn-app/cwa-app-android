package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.recovery.RecoveryQrCodeTestData
import de.rki.coronawarnapp.di.DiTestProvider
import io.kotest.matchers.shouldNotBe
import io.mockk.coVerify
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RecoveryCertificateContainerTest : BaseTest() {

    private val extractor: DccQrCodeExtractor = DiTestProvider.extractor
    private lateinit var extractorSpy: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        extractorSpy = spyk(extractor)
    }

    @Test
    fun `default parsing mode for containers is lenient`() {
        val container = RecoveryCertificateContainer(
            data = StoredRecoveryCertificateData(
                recoveryCertificateQrCode = RecoveryQrCodeTestData.recoveryQrCode2
            ),
            qrCodeExtractor = extractorSpy
        )

        container.qrCodeHash shouldNotBe null
        container.personIdentifier shouldNotBe null

        coVerify {
            extractorSpy.extract(RecoveryQrCodeTestData.recoveryQrCode2, DccV1Parser.Mode.CERT_REC_LENIENT)
        }
    }
}
