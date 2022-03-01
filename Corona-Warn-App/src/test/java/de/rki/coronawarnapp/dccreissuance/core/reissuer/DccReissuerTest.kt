package de.rki.coronawarnapp.dccreissuance.core.reissuer

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateRef
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.ReissuanceDivision
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.processor.DccReissuanceProcessor
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DccReissuerTest : BaseTest() {
    @MockK lateinit var dccSwapper: DccSwapper
    @MockK lateinit var dccReissuanceProcessor: DccReissuanceProcessor

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
        )
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { dccSwapper.swap(any(), any()) } just Runs
        coEvery { dccReissuanceProcessor.requestDccReissuance(any()) } returns DccReissuanceResponse(
            dccReissuances = emptyList()
        )
    }

    @Test
    fun `startReissuance throws DCC_RI_NO_RELATION if no reissuances`() = runBlockingTest {
        shouldThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION

        coVerify(exactly = 0) { dccSwapper.swap(any(), any()) }
    }

    @Test
    fun `startReissuance throws DCC_RI_NO_RELATION if no relation index`() = runBlockingTest {
        coEvery { dccReissuanceProcessor.requestDccReissuance(any()) } returns DccReissuanceResponse(
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

        coVerify(exactly = 0) { dccSwapper.swap(any(), any()) }
    }

    @Test
    fun `startReissuance throws DCC_RI_NO_RELATION if no relation action`() = runBlockingTest {
        coEvery { dccReissuanceProcessor.requestDccReissuance(any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(
                DccReissuanceResponse.DccReissuance(
                    certificate = "HC1:6BFOXN...",
                    relations = listOf(
                        DccReissuanceResponse.Relation(
                            index = 1,
                            action = "combine"
                        )
                    )
                )
            )
        )
        shouldThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_NO_RELATION

        coVerify(exactly = 0) { dccSwapper.swap(any(), any()) }
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
        coEvery { dccReissuanceProcessor.requestDccReissuance(any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(dccReissuance)
        )
        shouldNotThrow<DccReissuanceException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }

        coVerify(exactly = 1) {
            dccSwapper.swap(
                dccReissuance,
                certificateReissuance.certificateToReissue
            )
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
        coEvery { dccReissuanceProcessor.requestDccReissuance(any()) } returns DccReissuanceResponse(
            dccReissuances = listOf(dccReissuance)
        )

        coEvery { dccSwapper.swap(dccReissuance, certificateReissuance.certificateToReissue) } throws
            InvalidHealthCertificateException(
                errorCode = InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
            )
        shouldThrow<InvalidHealthCertificateException> {
            dccReissuer().startReissuance(dccReissuanceDescriptor = certificateReissuance)
        }.errorCode shouldBe InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
    }

    private fun dccReissuer() = DccReissuer(
        dccSwapper = dccSwapper,
        dccReissuanceProcessor = dccReissuanceProcessor
    )
}
