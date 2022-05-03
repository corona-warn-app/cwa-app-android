package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateRef
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuanceItem
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.ReissuanceDivision
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DccReissuerTest : BaseTest() {

    @MockK lateinit var dccReissuanceServer: DccReissuanceServer
    @MockK lateinit var dccQrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var vcRepo: VaccinationCertificateRepository
    @MockK lateinit var tcRepo: TestCertificateRepository
    @MockK lateinit var rcRepo: RecoveryCertificateRepository

    private val certificateReissuance = CertificateReissuance(
        reissuanceDivision = ReissuanceDivision(
            visible = true,
            titleText = SingleText(
                type = "string",
                localizedText = mapOf("de" to "Zertifikat ersetzen"),
                parameters = listOf()
            ),
            subtitleText = SingleText(
                type = "string",
                localizedText = mapOf("de" to "Text"),
                parameters = listOf()
            ),
            longText = SingleText(
                type = "string",
                localizedText = mapOf("de" to "Langer Text"),
                parameters = listOf()
            ),
            faqAnchor = "dcc_admission_state"
        ),

        certificates = listOf(
            CertificateReissuanceItem(
                certificateToReissue = Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = "HC1:6BFOXN...",
                    )
                ),
                accompanyingCertificates = listOf(
                    Certificate(
                        certificateRef = CertificateRef(
                            barcodeData = "HC1:6BFOXN..."
                        )
                    )
                ),
                action = "renew"
            )
        )
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { dccReissuanceServer.requestDccReissuance(any(), any()) } returns DccReissuanceResponse(
            dccReissuances = emptyList()
        )
    }

    @Test
    fun `startReissuance throws DCC_RI_NO_RELATION if no reissuances`() = runBlockingTest {
        shouldThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION
    }

    @Test
    fun `startReissuance throws DCC_RI_NO_RELATION if no relation index`() = runBlockingTest {
        coEvery { dccReissuanceServer.requestDccReissuance("renew", any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(
                DccReissuanceResponse.DccReissuance(
                    certificate = "HC1:6BFOXN...",
                    relations = listOf(
                        DccReissuanceResponse.Relation(
                            index = 1,
                            action = "replace"
                        )
                    )
                )
            )
        )
        shouldThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION
    }

    @Test
    fun `startReissuance throws DCC_RI_NO_RELATION if no relation action`() = runBlockingTest {
        coEvery { dccReissuanceServer.requestDccReissuance("renew", any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(
                DccReissuanceResponse.DccReissuance(
                    certificate = "HC1:6BFOXN...",
                    relations = listOf(
                        DccReissuanceResponse.Relation(
                            index = 0,
                            action = "combine"
                        )
                    )
                )
            )
        )
        shouldThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION
    }

    @Test
    fun `startReissuance works`() = runBlockingTest {
        val dccReissuance = DccReissuanceResponse.DccReissuance(
            certificate = "HC1:6BFOXN...",
            relations = listOf(
                DccReissuanceResponse.Relation(
                    index = 0,
                    action = "replace"
                )
            )
        )
        coEvery { dccReissuanceServer.requestDccReissuance("renew", any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(dccReissuance)
        )
        shouldNotThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }

        coVerify(exactly = 1) {
 //           vcRepo.recycleCertificate()
//            dccSwapper.swap(
//                dccReissuance,
//                certificateReissuance.certificateToReissue
//            )
        }
    }

    @Test
    fun `startReissuance should throw what swapper throws`() = runBlockingTest {
        val dccReissuance = DccReissuanceResponse.DccReissuance(
            certificate = "HC1:6BFOXN...",
            relations = listOf(
                DccReissuanceResponse.Relation(
                    index = 0,
                    action = "replace"
                )
            )
        )
        coEvery { dccReissuanceServer.requestDccReissuance("renew", any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(dccReissuance)
        )

//        coEvery { dccSwapper.swap(dccReissuance, certificateReissuance.certificateToReissue) } throws
//            InvalidHealthCertificateException(
//                errorCode = InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
//            )
        shouldThrow<InvalidHealthCertificateException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
    }

    private val testCertificateToReissue = Certificate(
        certificateRef = CertificateRef(
            barcodeData = "HC1:6789...",
        )
    )

    private val testAccompanyingCertificate1 = Certificate(
        certificateRef = CertificateRef(
            barcodeData = "HC1:1235....",
        )
    )

    private val testAccompanyingCertificate2 = Certificate(
        certificateRef = CertificateRef(
            barcodeData = "HC1:ABCD...",
        )
    )

    private val dccReissuanceDescriptor: CertificateReissuance = mockk {
        every { certificateToReissue } returns testCertificateToReissue
        every { accompanyingCertificates } returns listOf(
            testAccompanyingCertificate1,
            testAccompanyingCertificate2
        )
    }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `maps input correctly and forwards response`() = runBlockingTest {
        val response = DccReissuanceResponse(dccReissuances = emptyList())

        coEvery { dccReissuanceServer.requestDccReissuance(any(), any()) } returns response

        dccReissuer().startReissuance(dccReissuanceDescriptor = dccReissuanceDescriptor) shouldBe response

        coVerify {
            dccReissuanceServer.requestDccReissuance(
                action = "renew",
                certificates = listOf(
                    testCertificateToReissue.certificateRef.barcodeData,
                    testAccompanyingCertificate1.certificateRef.barcodeData,
                    testAccompanyingCertificate2.certificateRef.barcodeData
                )
            )
        }
    }

    @Test
    fun `forwards errors`() = runBlockingTest {
        val errorCode = DccReissuanceException.ErrorCode.DCC_RI_400
        coEvery { dccReissuanceServer.requestDccReissuance(any(), any()) } throws DccReissuanceException(
            errorCode = errorCode
        )

        shouldThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = dccReissuanceDescriptor)
        }.errorCode shouldBe errorCode
    }

    private fun dccReissuer() = DccReissuer(
        dccReissuanceServer = dccReissuanceServer,
        dccQrCodeExtractor,
        vcRepo,
        tcRepo,
        rcRepo,
    )
}
