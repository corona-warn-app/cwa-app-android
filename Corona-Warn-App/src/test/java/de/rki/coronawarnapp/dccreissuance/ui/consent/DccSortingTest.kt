package de.rki.coronawarnapp.dccreissuance.ui.consent

import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts.sort
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccSortingTest : BaseTest() {

    @MockK lateinit var vacQrCode: VaccinationCertificateQRCode
    @MockK lateinit var recQrCode: RecoveryCertificateQRCode
    @MockK lateinit var testQrCode: TestCertificateQRCode

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `sort works`() {
        every { vacQrCode.data.certificate.vaccination.vaccinatedOn } returns LocalDate.parse("2022-01-01")
        every { recQrCode.data.certificate.recovery.testedPositiveOn } returns LocalDate.parse("2022-01-03")
        every { testQrCode.data.certificate.test.sampleCollectedAt } returns Instant.parse("2022-01-05")
        listOf(vacQrCode, testQrCode, recQrCode).sort() shouldBe listOf(testQrCode, recQrCode, vacQrCode)
    }

    @Test
    fun `sort works 2`() {
        every { vacQrCode.data.certificate.vaccination.vaccinatedOn } returns LocalDate.parse("2022-02-01")
        every { recQrCode.data.certificate.recovery.testedPositiveOn } returns LocalDate.parse("2022-01-03")
        every { testQrCode.data.certificate.test.sampleCollectedAt } returns Instant.parse("2022-03-05")
        listOf(vacQrCode, testQrCode, recQrCode).sort() shouldBe listOf(testQrCode, vacQrCode, recQrCode)
    }
}
