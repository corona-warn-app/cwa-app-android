package de.rki.coronawarnapp.covidcertificate.pdf.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.LocalDate

class DccExportFilterTest : BaseTest() {

    private val vaccination1 = mockk<VaccinationCertificate>().apply {
        every { fullName } returns "Alpha"
        every { vaccinatedOn } returns LocalDate.parse("2022-03-10")
        every { state } returns CwaCovidCertificate.State.ExpiringSoon(Instant.parse("2022-05-09T10:00:00Z"))
    }

    private val vaccination2 = mockk<VaccinationCertificate>().apply {
        every { fullName } returns "Alpha"
        every { vaccinatedOn } returns LocalDate.parse("2022-04-10")
        every { state } returns CwaCovidCertificate.State.Valid(Instant.parse("2022-05-09T10:00:00Z"))
    }

    private val recovery1 = mockk<RecoveryCertificate>().apply {
        every { fullName } returns "Beta"
        every { testedPositiveOn } returns LocalDate.parse("2022-03-10")
        every { state } returns CwaCovidCertificate.State.Invalid(false)
    }

    private val recovery2 = mockk<RecoveryCertificate>().apply {
        every { fullName } returns "Beta"
        every { testedPositiveOn } returns LocalDate.parse("2022-03-10")
        every { state } returns CwaCovidCertificate.State.Recycled
    }

    private val test1 = mockk<TestCertificate>().apply {
        every { fullName } returns "Alpha"
        every { sampleCollectedAt } returns Instant.parse("2022-03-10T10:00:00Z")
        every { state } returns CwaCovidCertificate.State.Expired(Instant.parse("2022-05-09T10:00:00Z"))
    }

    private val test2 = mockk<TestCertificate>().apply {
        every { fullName } returns "Ceta"
        every { sampleCollectedAt } returns Instant.parse("2022-05-09T10:00:00Z")
        every { state } returns CwaCovidCertificate.State.Expired(Instant.parse("2022-05-09T10:00:00Z"))
    }

    @Test
    fun `filter works`() {
        listOf(recovery1, test1, vaccination1, test2, vaccination2, recovery2).filterAndSortForExport(
            Instant.parse("2022-05-10T10:00:00Z")
        ) shouldBe listOf(
            vaccination1, vaccination2, recovery1, test2
        )
    }
}
