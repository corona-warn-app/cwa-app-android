package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PersonCertificatesExtensionsTest : BaseTest() {

    private val time = Instant.now()
    private val oneDayDuration = Duration.standardDays(1)

    @Test
    fun `certificate sort order`() {

        val certificateFirst = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns time.plus(oneDayDuration).toLocalDateUtc()
        }

        val certificateFirstIssuedAtAnotherDate = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-07-20T10:00:00.000Z")
            every { vaccinatedOn } returns time.plus(oneDayDuration).toLocalDateUtc()
        }

        val certificateSecond = mockk<TestCertificate>().apply {
            every { sampleCollectedAt } returns time
        }

        val certificateThird = mockk<RecoveryCertificate>().apply {
            every { validFrom } returns time.minus(oneDayDuration).toLocalDateUtc()
        }

        val expectedOrder =
            listOf(certificateFirst, certificateFirstIssuedAtAnotherDate, certificateSecond, certificateThird)
        val wrongOrder =
            listOf(certificateSecond, certificateFirst, certificateThird, certificateFirstIssuedAtAnotherDate)
        val wrongOrder2 =
            listOf(certificateThird, certificateSecond, certificateFirstIssuedAtAnotherDate, certificateFirst)

        expectedOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder.toCertificateSortOrder() shouldBe expectedOrder
        wrongOrder2.toCertificateSortOrder() shouldBe expectedOrder

        wrongOrder shouldNotBe expectedOrder
        wrongOrder2 shouldNotBe expectedOrder
    }

    @Test
    fun `certificate sort order -  crash EXPOSUREAPP-9880`() {
        val vc1 = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns Instant.parse("2021-06-24T14:00:00.000Z").toLocalDateUtc()
        }

        val vc2 = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-09-20T10:00:00.000Z")
            every { vaccinatedOn } returns Instant.parse("2021-04-25T14:00:00.000Z").toLocalDateUtc()
        }

        val rc1 = mockk<RecoveryCertificate>().apply {
            every { validFrom } returns Instant.parse("2021-04-25T14:00:00.000Z").toLocalDateUtc()
        }

        val tc2 = mockk<TestCertificate>().apply {
            every { sampleCollectedAt } returns Instant.parse("2021-08-23T14:00:00.000Z")
        }

        shouldNotThrowAny {
            listOf(vc1, vc2, rc1, tc2).toCertificateSortOrder()
        }
    }

    @Suppress("LongMethod", "ComplexMethod")
    @Test
    fun `determine Highest Priority Certificate`() {
        // RAT certificate > 24 hours
        val first = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(25))
            every { getState() } returns mockk<State.Expired>()
        }
        // RAT certificate > 24 hours invalid
        val firstInvalid = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(25))
            every { getState() } returns mockk<State.Invalid>()
        }
        // PCR certificate > 48 hours
        val second = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(49))
            every { getState() } returns mockk<State.Expired>()
        }
        // PCR certificate > 48 hours invalid
        val secondInvalid = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(49))
            every { getState() } returns mockk<State.Expired>()
        }
        // Recovery certificate > 180 days
        val third = mockk<RecoveryCertificate>().apply {
            every { rawCertificate.recovery.validFrom } returns time.minus(Duration.standardDays(181)).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate with dose 1/2 and > 14 days since administration
        val fourth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(25)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns false
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate with dose 1/2 and < 14 days since administration
        val fifth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(1)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns false
            every { getState() } returns mockk<State.Valid>()
        }
        // Recovery certificate < 180 days old
        val sixth = mockk<RecoveryCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardDays(10))
            every { rawCertificate.recovery.validFrom } returns time.minus(Duration.standardDays(10))
                .toLocalDateUtc()
            every { rawCertificate.recovery.testedPositiveOn } returns time.minus(Duration.standardDays(30))
                .toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type J&J and < 14 days old
        val seventh = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 1
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(1)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1525"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type J&J and > 14 days old
        val eighth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardDays(18))
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 1
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(18)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1525"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type Pfizer/Moderna/AZ with dose 1/1 and < 14 days old
        val ninth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 1
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(1)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type Pfizer/Moderna/AZ with dose 2/2 < 14 days old
        val tenth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(1)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type Pfizer/Moderna/AZ with dose 2/2 and > 14 days old
        val eleventh = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardDays(16))
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(16)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type Pfizer/Moderna/AZ with dose 1/1 and > 14 days old
        val twelfth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardDays(17))
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 1
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(17)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type Pfizer/Moderna/AZ with dose 3/3 < 14 days old
        val thirteenth = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time
            every { rawCertificate.vaccination.doseNumber } returns 3
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 3
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(1)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type Pfizer/Moderna/AZ with dose 3/3 < 14 days old issue at a different time
        val thirteenthDifferentTime = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardDays(1))
            every { rawCertificate.vaccination.doseNumber } returns 3
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 3
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(1)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // RAT test < 24 hours old
        val fourteenth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(14))
            every { getState() } returns mockk<State.Valid>()
        }
        // PCR test < 48 hours old
        val fifteenth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(30))
            every { getState() } returns mockk<State.Valid>()
        }

        val certificatesList = mutableListOf<CwaCovidCertificate>()

        shouldThrow<NoSuchElementException> {
            certificatesList.findHighestPriorityCertificate(time)
        }

        // Start scanning the certificates one by one, starting with RAT > 24 hours old
        certificatesList.add(first)
        certificatesList.findHighestPriorityCertificate() shouldBe first
        // Add RAT > 24 hours but invalid
        certificatesList.add(firstInvalid)
        certificatesList.findHighestPriorityCertificate() shouldBe first
        // Add PCR > 48 hours old
        certificatesList.add(second)
        certificatesList.findHighestPriorityCertificate() shouldBe second
        // Add PCR > 48 hours old but invalid
        certificatesList.add(secondInvalid)
        certificatesList.findHighestPriorityCertificate() shouldBe second
        // Add Recovery > 180 days old
        certificatesList.add(third)
        certificatesList.findHighestPriorityCertificate() shouldBe third
        // Add  Vaccination 1/2 and > 14 days
        certificatesList.add(fourth)
        certificatesList.findHighestPriorityCertificate() shouldBe fourth
        // Add Vaccination 1/2 and < 14 days
        certificatesList.add(fifth)
        certificatesList.findHighestPriorityCertificate() shouldBe fifth
        // Add Recovery < 180 days
        certificatesList.add(sixth)
        certificatesList.findHighestPriorityCertificate() shouldBe sixth
        // Add J&J vaccine < 14 days, after a recovery certificate should still give recovery certificate priority
        certificatesList.add(seventh)
        certificatesList.findHighestPriorityCertificate() shouldBe sixth
        // Add J&J vaccine > 14 days, after a recovery certificate should give vaccine priority
        certificatesList.add(eighth)
        certificatesList.findHighestPriorityCertificate() shouldBe eighth
        // Add Pfizer/Moderna/AZ vaccine 1/1 < 14 days, after a recovery certificate should give vaccine priority
        certificatesList.add(ninth)
        certificatesList.findHighestPriorityCertificate() shouldBe ninth
        // Cleaning up certificates
        certificatesList.remove(sixth)
        certificatesList.remove(seventh)
        certificatesList.remove(ninth)
        // Add Pfizer/Moderna/AZ vaccine 2/2 < 14 days after J&J vaccine 1/1, priority should still be J&J
        certificatesList.add(tenth)
        certificatesList.findHighestPriorityCertificate() shouldBe eighth
        // Add Pfizer/Moderna/AZ vaccine 2/2 > 14 days after J&J vaccine 1/1
        certificatesList.add(eleventh)
        certificatesList.findHighestPriorityCertificate() shouldBe eleventh
        // Cleaning up certificates
        certificatesList.remove(eighth)
        certificatesList.remove(eleventh)
        // Add Pfizer/Moderna/AZ 1/1 > 14 days compared to the tenth vaccine.
        certificatesList.add(twelfth)
        certificatesList.findHighestPriorityCertificate() shouldBe twelfth
        // Add Pfizer/Moderna/AZ 2/2 > 14 days after Pfizer/Moderna/AZ 1/1 > 14 days
        certificatesList.add(eleventh)
        certificatesList.findHighestPriorityCertificate() shouldBe eleventh
        // Add Pfizer/Moderna/AZ 3/3 < 14 days after Pfizer/Moderna/AZ 2/2 > 14 days
        certificatesList.add(thirteenth)
        certificatesList.findHighestPriorityCertificate() shouldBe thirteenth
        // Add Pfizer/Moderna/AZ 3/3 < 14 days after Pfizer/Moderna/AZ 3/3 < 14 days, issued at 1 day in sooner
        certificatesList.add(thirteenthDifferentTime)
        certificatesList.findHighestPriorityCertificate() shouldBe thirteenth
        // Add RAT test < 24 hours old
        certificatesList.add(fourteenth)
        certificatesList.findHighestPriorityCertificate() shouldBe fourteenth
        // Add PCR test < 48 hours old
        certificatesList.add(fifteenth)
        certificatesList.findHighestPriorityCertificate() shouldBe fifteenth
    }

    /**
     * Bad: listOf(null,null,cert).map {...}.firstOrNull() ?: fallback
     *
     * findHighestPriorityCertificate(nowUtc=2021-06-24T14:00:00.000Z): [VaccinationCertificate(#1), VaccinationCertificate(#7)]
     * No certs with state Valid/ExpiringSoon
     * Checking 2 certs with for Expired
     * Rule 3 match (Series-completing Vaccination Certificate > 14 days): VaccinationCertificate(#7)
     * No certs with state Invalid
     * No priority match, this should not happen: [VaccinationCertificate(#1), VaccinationCertificate(#7)]
     *
     *
     * Good: listOf(null,null,cert).mapNotNull {...}.firstOrNull() ?: fallback
     *
     * findHighestPriorityCertificate(nowUtc=2021-06-24T14:00:00.000Z): [VaccinationCertificate(#1), VaccinationCertificate(#7)]
     * No certs with state Valid/ExpiringSoon
     * Checking 2 certs with for Expired
     * Rule 3 match (Series-completing Vaccination Certificate > 14 days): VaccinationCertificate(#7)
     * No certs with state Invalid
     */
    @Test
    fun `fallback behavior when there are no valid certificates`() {
        val first = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-01-20T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-01")
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1507"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Expired>()
        }

        val second = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns Instant.parse("2021-02-20T14:00:00.000Z")
            every { rawCertificate.vaccination.doseNumber } returns 2
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.vaccinatedOn } returns LocalDate.parse("2021-01-02")
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1507"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Expired>()
        }

        listOf(first, second).findHighestPriorityCertificate(time) shouldBe second
    }
}
