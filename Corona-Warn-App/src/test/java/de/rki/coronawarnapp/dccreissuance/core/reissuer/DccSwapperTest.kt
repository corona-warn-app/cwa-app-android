package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateRef
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DccSwapperTest : BaseTest() {
    @MockK lateinit var dccQrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var vcRepo: VaccinationCertificateRepository
    @MockK lateinit var tcRepo: TestCertificateRepository
    @MockK lateinit var rcRepo: RecoveryCertificateRepository

    private val dccReissuance = DccReissuanceResponse.DccReissuance(
        certificate = "HC1:6BFOXN...",
        relations = listOf()
    )

    private val certificateToReissue = Certificate(
        certificateRef = CertificateRef(
            barcodeData = "HC1:6BFOXN...",
        )
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { vcRepo.replaceCertificate(any(), any()) } just Runs
        coEvery { tcRepo.replaceCertificate(any(), any()) } just Runs
        coEvery { rcRepo.replaceCertificate(any(), any()) } just Runs
    }

    @Test
    fun `swap vc`() = runTest {
        val qrCode = mockk<VaccinationCertificateQRCode>()
        coEvery { dccQrCodeExtractor.extract(any()) } returns qrCode
        dccSwapper().swap(dccReissuance, certificateToReissue)
        coVerify(exactly = 1) {
            vcRepo.replaceCertificate(
                VaccinationCertificateContainerId(dccReissuance.certificate.toSHA256()),
                qrCode
            )
        }

        coVerify(exactly = 0) {
            rcRepo.replaceCertificate(any(), any())
            tcRepo.replaceCertificate(any(), any())
        }
    }

    @Test
    fun `swap tc`() = runTest {
        val qrCode = mockk<TestCertificateQRCode>()
        coEvery { dccQrCodeExtractor.extract(any()) } returns qrCode
        dccSwapper().swap(dccReissuance, certificateToReissue)
        coVerify(exactly = 1) {
            tcRepo.replaceCertificate(
                TestCertificateContainerId(dccReissuance.certificate.toSHA256()),
                qrCode
            )
        }

        coVerify(exactly = 0) {
            rcRepo.replaceCertificate(any(), any())
            vcRepo.replaceCertificate(any(), any())
        }
    }

    @Test
    fun `swap rc`() = runTest {
        val qrCode = mockk<RecoveryCertificateQRCode>()
        coEvery { dccQrCodeExtractor.extract(any()) } returns qrCode
        dccSwapper().swap(dccReissuance, certificateToReissue)
        coVerify(exactly = 1) {
            rcRepo.replaceCertificate(
                RecoveryCertificateContainerId(dccReissuance.certificate.toSHA256()),
                qrCode
            )
        }

        coVerify(exactly = 0) {
            tcRepo.replaceCertificate(any(), any())
            vcRepo.replaceCertificate(any(), any())
        }
    }

    private fun dccSwapper() = DccSwapper(
        dccQrCodeExtractor = dccQrCodeExtractor,
        vcRepo = vcRepo,
        tcRepo = tcRepo,
        rcRepo = rcRepo
    )
}
