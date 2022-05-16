package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateRef
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuanceItem
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.ReissuanceDivision
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccReissuerTest : BaseTest() {

    @MockK lateinit var dccReissuanceServer: DccReissuanceServer
    @MockK lateinit var dccQrCodeHandler: DccQrCodeHandler
    @Inject lateinit var dccQrCodeExtractor: DccQrCodeExtractor

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
                        barcodeData = VaccinationTestData.personAVac1QRCodeString,
                    )
                ),
                accompanyingCertificates = listOf(
                    Certificate(
                        certificateRef = CertificateRef(
                            barcodeData = VaccinationTestData.personAVac2QRCodeString
                        )
                    )
                ),
                action = ACTION_RENEW
            )
        )
    )

    private val certificateReissuanceLegacy = CertificateReissuance(
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
        certificateToReissue = Certificate(
            certificateRef = CertificateRef(
                barcodeData = VaccinationTestData.personAVac1QRCodeString,
            )
        ),
        accompanyingCertificates = listOf(
            Certificate(
                certificateRef = CertificateRef(
                    barcodeData = VaccinationTestData.personAVac2QRCodeString
                )
            )
        )
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        coEvery { dccReissuanceServer.requestDccReissuance(any(), any()) } returns DccReissuanceResponse(
            dccReissuances = emptyList()
        )
        coEvery {
            dccQrCodeHandler.register(any())
        } returns VaccinationCertificateContainerId("hash")
        coEvery {
            dccQrCodeHandler.moveToRecycleBin(any())
        } just Runs
    }

    @Test
    fun `startReissuance works`() = runTest {
        val dccReissuance = DccReissuanceResponse.DccReissuance(
            certificate = VaccinationTestData.personAVac1QRCodeString,
            relations = listOf(
                DccReissuanceResponse.Relation(
                    index = 1,
                    action = ACTION_REPLACE
                )
            )
        )
        coEvery { dccReissuanceServer.requestDccReissuance(ACTION_RENEW, any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(dccReissuance)
        )
        shouldNotThrow<DccReissuanceException> {
            instance().startReissuance(certificateReissuance = certificateReissuance)
        }

        val qrCode2 = dccQrCodeExtractor.extract(VaccinationTestData.personAVac2QRCodeString)

        coVerify(exactly = 1) {
            dccReissuanceServer.requestDccReissuance(
                action = ACTION_RENEW,
                certificates = listOf(
                    VaccinationTestData.personAVac1QRCodeString,
                    VaccinationTestData.personAVac2QRCodeString
                )
            )
            dccQrCodeHandler.register(VaccinationTestData.personAVac1QRCode)
            dccQrCodeHandler.moveToRecycleBin(qrCode2)
        }
    }

    @Test
    fun `startReissuance works for legacy wallet`() = runTest {
        val dccReissuance = DccReissuanceResponse.DccReissuance(
            certificate = VaccinationTestData.personAVac1QRCodeString,
            relations = listOf(
                DccReissuanceResponse.Relation(
                    index = 0,
                    action = ACTION_REPLACE
                )
            )
        )
        coEvery { dccReissuanceServer.requestDccReissuance(ACTION_RENEW, any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(dccReissuance)
        )
        shouldNotThrow<DccReissuanceException> {
            instance().startReissuance(certificateReissuance = certificateReissuanceLegacy)
        }

        coVerify(exactly = 1) {
            dccReissuanceServer.requestDccReissuance(
                action = ACTION_RENEW,
                certificates = listOf(
                    VaccinationTestData.personAVac1QRCodeString,
                    VaccinationTestData.personAVac2QRCodeString
                )
            )
            dccQrCodeHandler.register(VaccinationTestData.personAVac1QRCode)
            dccQrCodeHandler.moveToRecycleBin(VaccinationTestData.personAVac1QRCode)
        }
    }

    @Test
    fun `startReissuance should throw what handler throws`() = runTest {
        val dccReissuance = DccReissuanceResponse.DccReissuance(
            certificate = VaccinationTestData.personAVac1QRCodeString,
            relations = listOf(
                DccReissuanceResponse.Relation(
                    index = 0,
                    action = ACTION_REPLACE
                )
            )
        )
        coEvery { dccReissuanceServer.requestDccReissuance(ACTION_RENEW, any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(dccReissuance)
        )

        coEvery { dccQrCodeHandler.register(any()) } throws
            InvalidHealthCertificateException(
                errorCode = InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
            )
        shouldThrow<InvalidHealthCertificateException> {
            instance().startReissuance(certificateReissuance = certificateReissuance)
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
    }

    @Test
    fun `forwards errors`() = runTest {
        val errorCode = DccReissuanceException.ErrorCode.DCC_RI_400
        coEvery { dccReissuanceServer.requestDccReissuance(any(), any()) } throws DccReissuanceException(
            errorCode = errorCode
        )

        shouldThrow<DccReissuanceException> {
            instance().startReissuance(certificateReissuance = certificateReissuance)
        }.errorCode shouldBe errorCode
    }

    private fun instance() = DccReissuer(
        dccReissuanceServer = dccReissuanceServer,
        dccQrCodeExtractor = dccQrCodeExtractor,
        dccQrCodeHandler = dccQrCodeHandler
    )
}
