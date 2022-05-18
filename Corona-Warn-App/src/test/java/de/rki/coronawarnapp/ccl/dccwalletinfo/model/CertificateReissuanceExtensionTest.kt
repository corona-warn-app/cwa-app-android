package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class CertificateReissuanceExtensionTest : BaseTest() {

    val reissuanceDivision: ReissuanceDivision = mockk()

    private val certificateReissuanceLegacy = CertificateReissuance(
        reissuanceDivision = reissuanceDivision,
        certificateToReissue = Certificate(
            certificateRef = CertificateRef(
                barcodeData = qrCodeStringVac,
            )
        ),
        accompanyingCertificates = listOf(
            Certificate(
                certificateRef = CertificateRef(
                    barcodeData = qrCodeStringTest
                )
            )
        )
    )

    private val certificateReissuance = CertificateReissuance(
        reissuanceDivision = reissuanceDivision,
        certificates = listOf(
            CertificateReissuanceItem(
                certificateToReissue = Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = qrCodeStringVac,
                    )
                ),
                accompanyingCertificates = listOf(
                    Certificate(
                        certificateRef = CertificateRef(
                            barcodeData = qrCodeStringTest
                        )
                    )
                ),
                action = "renew"
            ),
        )
    )

    private val certificateReissuance2 = CertificateReissuance(
        reissuanceDivision = mockk(),
        certificates = listOf(
            CertificateReissuanceItem(
                certificateToReissue = Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = qrCodeStringVac,
                    )
                ),
                accompanyingCertificates = listOf(
                    Certificate(
                        certificateRef = CertificateRef(
                            barcodeData = qrCodeStringTest
                        )
                    )
                ),
                action = "renew"
            ),
            CertificateReissuanceItem(
                certificateToReissue = Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = qrCodeStringTest,
                    )
                ),
                accompanyingCertificates = listOf(
                    Certificate(
                        certificateRef = CertificateRef(
                            barcodeData = qrCodeStringVac
                        )
                    )
                ),
                action = "extend"
            ),
        )
    )

    private val certificateReissuance3 = CertificateReissuance(
        reissuanceDivision = mockk(),
        certificates = listOf(
            CertificateReissuanceItem(
                certificateToReissue = Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = qrCodeStringVac,
                    )
                ),
                accompanyingCertificates = listOf(
                    Certificate(
                        certificateRef = CertificateRef(
                            barcodeData = qrCodeStringTest
                        )
                    )
                ),
                action = "renew"
            ),
            CertificateReissuanceItem(
                certificateToReissue = Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = qrCodeStringVac,
                    )
                ),
                accompanyingCertificates = listOf(
                    Certificate(
                        certificateRef = CertificateRef(
                            barcodeData = qrCodeStringTest
                        )
                    )
                ),
                action = "extend"
            ),
        )
    )

    @Test
    fun `compat test`() {
        certificateReissuanceLegacy.asCertificateReissuanceCompat() shouldBe
            certificateReissuance
    }

    @Test
    fun `certificate consolidation test`() {
        val set = certificateReissuance.consolidateAccompanyingCertificates()
        set shouldContainAll
            setOf(
                Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = qrCodeStringTest
                    )
                )
            )
        set.size shouldBe 1
    }

    @Test
    fun `certificate consolidation test with certificates which accompany each other`() {
        val set = certificateReissuance2.consolidateAccompanyingCertificates()
        set.size shouldBe 0
    }

    @Test
    fun `certificate consolidation test with duplicates`() {
        val set = certificateReissuance3.consolidateAccompanyingCertificates()
        set shouldContainAll
            setOf(
                Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = qrCodeStringTest
                    )
                )
            )
        set.size shouldBe 1
    }
}
