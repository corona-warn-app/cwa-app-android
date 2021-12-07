package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.Other
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithRAT
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoG
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusRAT
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
    fun `determine Highest Priority Certificate and AdmissionState`() {
        // RAT certificate > 48 hours
        val first = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(49))
            every { getState() } returns mockk<State.Expired>()
        }
        // RAT certificate > 48 hours invalid
        val firstInvalid = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(49))
            every { getState() } returns mockk<State.Invalid>()
        }
        // PCR certificate > 72 hours
        val second = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(73))
            every { getState() } returns mockk<State.Expired>()
        }
        // PCR certificate > 72 hours invalid
        val secondInvalid = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(73))
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
        // RAT test < 48 hours old
        val fourteenth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(47))
            every { getState() } returns mockk<State.Valid>()
        }
        // PCR test < 72 hours old
        val fifteenth = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(71))
            every { getState() } returns mockk<State.Valid>()
        }

        mutableListOf<CwaCovidCertificate>().apply {

            findHighestPriorityCertificate(time) shouldBe null
            determineAdmissionState(time) shouldBe null

            // Start scanning the certificates one by one, starting with RAT > 48 hours old
            add(first)
            findHighestPriorityCertificate() shouldBe first
            determineAdmissionState() shouldBe Other(first)

            // Add RAT > 48 hours but invalid
            add(firstInvalid)
            findHighestPriorityCertificate() shouldBe first
            determineAdmissionState() shouldBe Other(first)

            // Add PCR > 72 hours old
            add(second)
            findHighestPriorityCertificate() shouldBe second
            determineAdmissionState() shouldBe Other(second)

            // Add PCR > 72 hours old but invalid
            add(secondInvalid)
            findHighestPriorityCertificate() shouldBe second
            determineAdmissionState() shouldBe Other(second)

            // Add Recovery > 180 days old
            add(third)
            findHighestPriorityCertificate() shouldBe third
            determineAdmissionState() shouldBe Other(third)

            // Add  Vaccination 1/2 and > 14 days
            add(fourth)
            findHighestPriorityCertificate() shouldBe fourth
            determineAdmissionState() shouldBe Other(fourth)

            // Add Vaccination 1/2 and < 14 days
            add(fifth)
            findHighestPriorityCertificate() shouldBe fifth
            determineAdmissionState() shouldBe Other(fifth)

            // Add Recovery < 180 days
            add(sixth)
            findHighestPriorityCertificate() shouldBe sixth
            determineAdmissionState() shouldBe TwoG(sixth)

            // Add J&J vaccine < 14 days, after a recovery certificate should still give recovery certificate priority
            add(seventh)
            findHighestPriorityCertificate() shouldBe sixth
            determineAdmissionState() shouldBe TwoG(sixth)

            // Add J&J vaccine > 14 days, after a recovery certificate should give vaccine priority
            add(eighth)
            findHighestPriorityCertificate() shouldBe eighth
            determineAdmissionState() shouldBe TwoG(eighth)

            // Add Pfizer/Moderna/AZ vaccine 1/1 < 14 days, after a recovery certificate should give vaccine priority
            add(ninth)
            findHighestPriorityCertificate() shouldBe ninth
            determineAdmissionState() shouldBe TwoG(ninth)

            // Cleaning up certificates
            remove(sixth)
            remove(seventh)
            remove(ninth)

            // Add Pfizer/Moderna/AZ vaccine 2/2 < 14 days after J&J vaccine 1/1, priority should still be J&J
            add(tenth)
            findHighestPriorityCertificate() shouldBe eighth
            determineAdmissionState() shouldBe TwoG(eighth)

            // Add Pfizer/Moderna/AZ vaccine 2/2 > 14 days after J&J vaccine 1/1
            add(eleventh)
            findHighestPriorityCertificate() shouldBe eleventh
            determineAdmissionState() shouldBe TwoG(eleventh)

            // Cleaning up certificates
            remove(eighth)
            remove(eleventh)

            // Add Pfizer/Moderna/AZ 1/1 > 14 days compared to the tenth vaccine.
            add(twelfth)
            findHighestPriorityCertificate() shouldBe twelfth
            determineAdmissionState() shouldBe TwoG(twelfth)

            // Add Pfizer/Moderna/AZ 2/2 > 14 days after Pfizer/Moderna/AZ 1/1 > 14 days
            add(eleventh)
            findHighestPriorityCertificate() shouldBe eleventh
            determineAdmissionState() shouldBe TwoG(eleventh)

            // Add Pfizer/Moderna/AZ 3/3 < 14 days after Pfizer/Moderna/AZ 2/2 > 14 days
            add(thirteenth)
            findHighestPriorityCertificate() shouldBe thirteenth
            determineAdmissionState() shouldBe TwoG(thirteenth)

            // Add Pfizer/Moderna/AZ 3/3 < 14 days after Pfizer/Moderna/AZ 3/3 < 14 days, issued at 1 day in sooner
            add(thirteenthDifferentTime)
            findHighestPriorityCertificate() shouldBe thirteenth
            determineAdmissionState() shouldBe TwoG(thirteenth)

            // Add RAT test < 48 hours old
            add(fourteenth)
            findHighestPriorityCertificate() shouldBe fourteenth
            determineAdmissionState() shouldBe TwoGPlusRAT(thirteenth, fourteenth)

            // Add PCR test < 72 hours old
            add(fifteenth)
            findHighestPriorityCertificate() shouldBe fifteenth
            determineAdmissionState() shouldBe TwoGPlusPCR(thirteenth, fifteenth)

            // Remove all Certificates
            clear()

            // Add RAT test < 48 hours old
            add(fourteenth)
            determineAdmissionState() shouldBe ThreeGWithRAT(fourteenth)

            // Add PCR test < 72 hours old
            add(fifteenth)
            determineAdmissionState() shouldBe ThreeGWithPCR(fifteenth)
        }
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
