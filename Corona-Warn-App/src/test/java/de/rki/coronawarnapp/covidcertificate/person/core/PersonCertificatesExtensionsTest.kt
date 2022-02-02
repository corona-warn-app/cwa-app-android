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
        val expiredRat = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(49))
            every { getState() } returns mockk<State.Expired>()
        }
        // RAT certificate > 48 hours invalid
        val invalidRat = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(49))
            every { getState() } returns mockk<State.Invalid>()
        }
        // PCR certificate > 72 hours
        val expiredPcr = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(73))
            every { getState() } returns mockk<State.Expired>()
        }
        // PCR certificate > 72 hours invalid
        val invalidPcr = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(73))
            every { getState() } returns mockk<State.Invalid>()
        }
        // Recovery certificate > 180 days
        val oldRecovery = mockk<RecoveryCertificate>().apply {
            every { rawCertificate.recovery.validFrom } returns time.minus(Duration.standardDays(181)).toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate with dose 1/2 and > 14 days since administration
        val incompleteVaccination = mockk<VaccinationCertificate>().apply {
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
        val incompleteTooRecentVaccination = mockk<VaccinationCertificate>().apply {
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
        val recentRecovery = mockk<RecoveryCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardDays(10))
            every { rawCertificate.recovery.validFrom } returns time.minus(Duration.standardDays(10))
                .toLocalDateUtc()
            every { rawCertificate.recovery.testedPositiveOn } returns time.minus(Duration.standardDays(30))
                .toLocalDateUtc()
            every { getState() } returns mockk<State.Valid>()
        }
        // Vaccination certificate of type J&J and < 14 days old
        val tooRecentJJVaccination = mockk<VaccinationCertificate>().apply {
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
        val jjVacination = mockk<VaccinationCertificate>().apply {
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
        val tooRecentCompleteVaccinationDose1Of1 = mockk<VaccinationCertificate>().apply {
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
        val tooRecentVaccinationDose2Of2 = mockk<VaccinationCertificate>().apply {
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
        val vaccinationDose2Of2 = mockk<VaccinationCertificate>().apply {
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
        val vaccinationDose1Of1 = mockk<VaccinationCertificate>().apply {
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
        val tooRecentVaccinationDose3Of3 = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardHours(23))
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
        val tooRecentVaccinationDose3Of3DifferentTime = mockk<VaccinationCertificate>().apply {
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
        // Vaccination certificate of type Pfizer/Moderna/AZ with dose 3/2 < 14 days old issue at a different time
        val tooRecentVaccinationDose3Of2DifferentTime = mockk<VaccinationCertificate>().apply {
            every { headerIssuedAt } returns time.minus(Duration.standardHours(22))
            every { rawCertificate.vaccination.doseNumber } returns 3
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every {
                rawCertificate.vaccination.vaccinatedOn
            } returns time.minus(Duration.standardDays(1)).toLocalDateUtc()
            every { rawCertificate.vaccination.medicalProductId } returns "EU/1/20/1528"
            every { isSeriesCompletingShot } returns true
            every { getState() } returns mockk<State.Valid>()
        }
        // RAT test < 48 hours old
        val recentRat = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(47))
            every { getState() } returns mockk<State.Valid>()
        }
        // PCR test < 72 hours old
        val recentPcr = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(71))
            every { getState() } returns mockk<State.Valid>()
        }
        // RAT test that expires soon
        val ratTestExpiringSoon = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP217198-3"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(1))
            every { getState() } returns mockk<State.ExpiringSoon>()
        }
        // PCR test that expires soon
        val pcrTestExpiringSoon = mockk<TestCertificate>().apply {
            every { rawCertificate.test.testType } returns "LP6464-4"
            every { rawCertificate.test.sampleCollectedAt } returns time.minus(Duration.standardHours(1))
            every { getState() } returns mockk<State.ExpiringSoon>()
        }

        mutableListOf<CwaCovidCertificate>().apply {

            findHighestPriorityCertificate(time) shouldBe null
            determineAdmissionState(time) shouldBe null

            // Start scanning the certificates one by one, starting with RAT > 48 hours old
            add(expiredRat)
            // certificates = expiredRat
            findHighestPriorityCertificate() shouldBe expiredRat
            determineAdmissionState() shouldBe Other(expiredRat)

            // Add RAT > 48 hours but invalid
            add(invalidRat)
            // certificates = expiredRat, invalidRat
            findHighestPriorityCertificate() shouldBe expiredRat
            determineAdmissionState() shouldBe Other(expiredRat)

            // Add PCR > 72 hours old
            add(expiredPcr)
            // certificates = expiredRat, invalidRat, expiredPcr
            findHighestPriorityCertificate() shouldBe expiredPcr
            determineAdmissionState() shouldBe Other(expiredPcr)

            // Add PCR > 72 hours old but invalid
            add(invalidPcr)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr
            findHighestPriorityCertificate() shouldBe expiredPcr
            determineAdmissionState() shouldBe Other(expiredPcr)

            // Add Recovery > 180 days old
            add(oldRecovery)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery
            findHighestPriorityCertificate() shouldBe oldRecovery
            determineAdmissionState() shouldBe Other(oldRecovery)

            // Add  Vaccination 1/2 and > 14 days
            add(incompleteVaccination)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination
            findHighestPriorityCertificate() shouldBe incompleteVaccination
            determineAdmissionState() shouldBe Other(incompleteVaccination)

            // Add Vaccination 1/2 and < 14 days
            add(incompleteTooRecentVaccination)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination
            findHighestPriorityCertificate() shouldBe incompleteTooRecentVaccination
            determineAdmissionState() shouldBe Other(incompleteTooRecentVaccination)

            // Add Recovery < 180 days
            add(recentRecovery)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, recentRecovery
            findHighestPriorityCertificate() shouldBe recentRecovery
            determineAdmissionState() shouldBe TwoG(recentRecovery)

            // Add J&J vaccine < 14 days, after a recovery certificate should still give recovery certificate priority
            add(tooRecentJJVaccination)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, recentRecovery, tooRecentJJVaccination
            findHighestPriorityCertificate() shouldBe recentRecovery
            determineAdmissionState() shouldBe TwoG(recentRecovery)

            // Add J&J vaccine > 14 days, after a recovery certificate should give vaccine priority
            add(jjVacination)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, recentRecovery, tooRecentJJVaccination, jjVaccination
            findHighestPriorityCertificate() shouldBe jjVacination
            determineAdmissionState() shouldBe TwoG(jjVacination)

            // Add Pfizer/Moderna/AZ vaccine 1/1 < 14 days, after a recovery certificate should give vaccine priority
            add(tooRecentCompleteVaccinationDose1Of1)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, recentRecovery, tooRecentJJVaccination, jjVaccination,
            // tooRecentCompleteVaccinationDose1Of1
            findHighestPriorityCertificate() shouldBe tooRecentCompleteVaccinationDose1Of1
            determineAdmissionState() shouldBe TwoG(tooRecentCompleteVaccinationDose1Of1)

            // Cleaning up certificates
            remove(recentRecovery)
            remove(tooRecentJJVaccination)
            remove(tooRecentCompleteVaccinationDose1Of1)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, jjVaccination,

            // Add Pfizer/Moderna/AZ vaccine 2/2 < 14 days after J&J vaccine 1/1, priority should still be J&J
            add(tooRecentVaccinationDose2Of2)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, jjVaccination, tooRecentVaccinationDose2Of2
            findHighestPriorityCertificate() shouldBe jjVacination
            determineAdmissionState() shouldBe TwoG(jjVacination)

            // Add Pfizer/Moderna/AZ vaccine 2/2 > 14 days after J&J vaccine 1/1
            add(vaccinationDose2Of2)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, jjVaccination, vaccinationDose2Of2
            findHighestPriorityCertificate() shouldBe vaccinationDose2Of2
            determineAdmissionState() shouldBe TwoG(vaccinationDose2Of2)

            // Cleaning up certificates
            remove(jjVacination)
            remove(vaccinationDose2Of2)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination

            // Add Pfizer/Moderna/AZ 1/1 > 14 days
            add(vaccinationDose1Of1)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, vaccinationDose1Of1
            findHighestPriorityCertificate() shouldBe vaccinationDose1Of1
            determineAdmissionState() shouldBe TwoG(vaccinationDose1Of1)

            // Add Pfizer/Moderna/AZ 2/2 > 14 days after Pfizer/Moderna/AZ 1/1 > 14 days
            add(vaccinationDose2Of2)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, vaccinationDose1Of1, vaccinationDose2Of2
            findHighestPriorityCertificate() shouldBe vaccinationDose2Of2
            determineAdmissionState() shouldBe TwoG(vaccinationDose2Of2)

            // Add Pfizer/Moderna/AZ 3/3 < 14 days after Pfizer/Moderna/AZ 2/2 > 14 days
            add(tooRecentVaccinationDose3Of3)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, vaccinationDose1Of1, vaccinationDose2Of2, tooRecentVaccinationDose3Of3
            findHighestPriorityCertificate() shouldBe tooRecentVaccinationDose3Of3
            determineAdmissionState() shouldBe TwoG(tooRecentVaccinationDose3Of3)

            // Add Pfizer/Moderna/AZ 3/3 < 14 days after Pfizer/Moderna/AZ 3/3 < 14 days, issued at 1 day in sooner
            add(tooRecentVaccinationDose3Of3DifferentTime)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, vaccinationDose1Of1, vaccinationDose2Of2, tooRecentVaccinationDose3Of3,
            // tooRecentVaccinationDose3Of3DifferentTime
            findHighestPriorityCertificate() shouldBe tooRecentVaccinationDose3Of3
            determineAdmissionState() shouldBe TwoG(tooRecentVaccinationDose3Of3)

            // Add Pfizer/Moderna/AZ 3/3 < 14 days after Pfizer/Moderna/AZ 3/3 < 14 days, issued at 1 day in sooner
            add(tooRecentVaccinationDose3Of2DifferentTime)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, vaccinationDose1Of1, vaccinationDose2Of2, tooRecentVaccinationDose3Of3,
            // tooRecentVaccinationDose3Of3DifferentTime, tooRecentVaccinationDose3Of2DifferentTime
            findHighestPriorityCertificate() shouldBe tooRecentVaccinationDose3Of2DifferentTime
            determineAdmissionState() shouldBe TwoG(tooRecentVaccinationDose3Of2DifferentTime)

            // Add RAT test < 48 hours old, vaccination certificate has priority over the rat test
            add(recentRat)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, vaccinationDose1Of1, vaccinationDose2Of2, tooRecentVaccinationDose3Of3,
            // tooRecentVaccinationDose3Of3DifferentTime, tooRecentVaccinationDose3Of2DifferentTime, recentRat
            findHighestPriorityCertificate() shouldBe tooRecentVaccinationDose3Of2DifferentTime
            determineAdmissionState() shouldBe TwoGPlusRAT(tooRecentVaccinationDose3Of2DifferentTime, recentRat)

            // Add PCR test < 72 hours old, vaccination certificate has priority over the pcr test
            add(recentPcr)
            // certificates = expiredRat, invalidRat, expiredPcr, invalidPcr, oldRecovery, incompleteVaccination,
            // incompleteTooRecentVaccination, vaccinationDose1Of1, vaccinationDose2Of2, tooRecentVaccinationDose3Of3,
            // tooRecentVaccinationDose3Of3DifferentTime, tooRecentVaccinationDose3Of2DifferentTime, recentRat, recentPcr
            findHighestPriorityCertificate() shouldBe tooRecentVaccinationDose3Of2DifferentTime
            determineAdmissionState() shouldBe TwoGPlusPCR(tooRecentVaccinationDose3Of2DifferentTime, recentPcr)

            // Remove all Certificates
            clear()

            // Add RAT test < 48 hours old
            add(recentRat)
            // certificates = recentRat
            findHighestPriorityCertificate() shouldBe recentRat
            determineAdmissionState() shouldBe ThreeGWithRAT(recentRat)

            // Add PCR test < 72 hours old
            add(recentPcr)
            // certificates = recentRat, recentPcr
            findHighestPriorityCertificate() shouldBe recentPcr
            determineAdmissionState() shouldBe ThreeGWithPCR(recentPcr)

            // Add recent recovery - should have priority over the tests
            add(recentRecovery)
            // certificates = recentRat, recentPcr, recentRecovery
            findHighestPriorityCertificate() shouldBe recentRecovery
            determineAdmissionState() shouldBe TwoGPlusPCR(recentRecovery, recentPcr)

            // Add full vaccination - should have priority over the recovery
            add(vaccinationDose2Of2)
            findHighestPriorityCertificate() shouldBe vaccinationDose2Of2
            // certificates = recentRat, recentPcr, recentRecovery, vaccinationDose2Of2
            determineAdmissionState() shouldBe TwoGPlusPCR(vaccinationDose2Of2, recentPcr)

            // Remove all Certificates
            clear()

            // Add RAT test that expires soon
            add(ratTestExpiringSoon)
            // certificates = ratTestExpiringSoon
            determineAdmissionState() shouldBe ThreeGWithRAT(ratTestExpiringSoon)

            // Add PCR test that expires soon
            add(pcrTestExpiringSoon)
            // certificates = ratTestExpiringSoon, pcrTestExpiringSoon
            determineAdmissionState() shouldBe ThreeGWithPCR(pcrTestExpiringSoon)
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
