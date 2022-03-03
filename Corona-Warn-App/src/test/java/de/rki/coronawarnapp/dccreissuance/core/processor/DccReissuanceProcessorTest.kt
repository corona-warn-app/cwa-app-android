package de.rki.coronawarnapp.dccreissuance.core.processor

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateRef
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServer
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
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

class DccReissuanceProcessorTest : BaseTest() {

    @MockK lateinit var dccReissuanceServer: DccReissuanceServer

    private val instance: DccReissuanceProcessor
        get() = DccReissuanceProcessor(dccReissuanceServer = dccReissuanceServer)

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

    private val testCertificateToReissue = Certificate(
        certificateRef = CertificateRef(
            barcodeData = "HC1:6789...",
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

        instance.requestDccReissuance(dccReissuanceDescriptor = dccReissuanceDescriptor) shouldBe response

        coVerify {
            dccReissuanceServer.requestDccReissuance(
                action = "renew",
                certificates = listOf(
                    testAccompanyingCertificate1.certificateRef.barcodeData,
                    testAccompanyingCertificate2.certificateRef.barcodeData,
                    testCertificateToReissue.certificateRef.barcodeData
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
            instance.requestDccReissuance(dccReissuanceDescriptor = dccReissuanceDescriptor)
        }.errorCode shouldBe errorCode
    }
}
