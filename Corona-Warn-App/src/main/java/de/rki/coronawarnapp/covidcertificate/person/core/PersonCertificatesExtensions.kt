package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.Other
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithRAT
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoG
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusRAT
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate.Companion.ONE_SHOT_VACCINES
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate.Companion.TWO_SHOT_VACCINES
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Days
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber

/*
    The list items shall be sorted descending by the following date attributes depending on the type of the DGC:
    for Vaccination Certificates (i.e. DGC with v[0]): the date of the vaccination v[0].dt and issuedAt date
    for Test Certificates (i.e. DGC with t[0]): the date of the sample collection t[0].sc
    for Recovery Certificates (i.e. DGC with r[0]): the date of begin of the validity r[0].df
 */
fun Collection<CwaCovidCertificate>.toCertificateSortOrder(): List<CwaCovidCertificate> {
    return this.sortedWith(
        compareBy(
            {
                when (it) {
                    is VaccinationCertificate -> it.vaccinatedOn
                    is TestCertificate -> it.sampleCollectedAt.toLocalDateUserTz()
                    is RecoveryCertificate -> it.validFrom
                    else -> throw IllegalStateException("Can't sort $it")
                }
            },
            {
                when (it) {
                    is VaccinationCertificate -> it.headerIssuedAt.toLocalDateUserTz()
                    is TestCertificate -> it.sampleCollectedAt.toLocalDateUserTz()
                    is RecoveryCertificate -> it.validFrom
                    else -> throw IllegalStateException("Can't sort $it")
                }
            }
        )
    ).reversed()
}

/**
 * 1
 * PCR Test Certificate <= 72 hours
 * Find Test Certificates (i.e. DGC with t[0]) where t[0].tt is set to LP6464-4 and the time difference between the
 * time represented by t[0].sc and the current device time is <= 72 hours, sorted descending by t[0].sc
 * (i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule1FindRecentPcrCertificate(
    nowUtc: Instant
): CwaCovidCertificate? = this
    .filterIsInstance<TestCertificate>()
    .filter { it.rawCertificate.test.testType == "LP6464-4" }
    .filter { Duration(it.rawCertificate.test.sampleCollectedAt, nowUtc) <= Duration.standardHours(72) }
    .maxByOrNull { it.rawCertificate.test.sampleCollectedAt }

/**
 * 2
 * RAT Test Certificate <= 48 hours
 * Find Test Certificates (i.e. DGC with t[0]) where t[0].tt is set to LP217198-3 and the time difference between
 * the time represented by t[0].sc and the current device time is <= 48 hours, sorted descending by t[0].sc
 * (i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule2FindRecentRaCertificate(
    nowUtc: Instant
): CwaCovidCertificate? = this
    .filterIsInstance<TestCertificate>()
    .filter { it.rawCertificate.test.testType == "LP217198-3" }
    .filter { Duration(it.rawCertificate.test.sampleCollectedAt, nowUtc) <= Duration.standardHours(48) }
    .maxByOrNull { it.rawCertificate.test.sampleCollectedAt }

/**
 * 3
 * Series-completing Vaccination Certificate:
 * Find Vaccination Certificates where total number of doses == number of administered doses and
 * 3.1 For vaccines with dose 3/3, priority will be received right away
 * 3.2 For BioNTech/Moderna/AstraZeneca vaccines that are taken after a recovery, priority will be received right away
 * 3.3 For J&J vaccines with dose 2/2, priority will be received right away
 * 3.4 If none of the criteria above is met, priority will be received after a 14 day period
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule3FindRecentLastShot(
    nowUtc: Instant
): CwaCovidCertificate? {
    val isOlderThanTwoWeeks = { certificate: VaccinationCertificate ->
        Days.daysBetween(
            certificate.rawCertificate.vaccination.vaccinatedOn,
            nowUtc.toLocalDateUtc()
        ).days > 14
    }
    return this
        .filterIsInstance<VaccinationCertificate>()
        .filter { it.isSeriesCompletingShot }
        .filter {
            with(it.rawCertificate.vaccination) {
                when {
                    totalSeriesOfDoses > 2 && medicalProductId in TWO_SHOT_VACCINES -> true
                    totalSeriesOfDoses == 2 && medicalProductId in ONE_SHOT_VACCINES -> true
                    totalSeriesOfDoses == 1 && TWO_SHOT_VACCINES.contains(medicalProductId) -> true
                    else -> isOlderThanTwoWeeks(it)
                }
            }
        }
        .maxWithOrNull(
            compareBy(
                { it.rawCertificate.vaccination.vaccinatedOn },
                { it.headerIssuedAt }
            )
        )
}

/**
 * 4
 * Recovery Certificate <= 180 days
 * Find Recovery Certificates (i.e. DGC with r[0]) where the time difference between the time
 * represented by r[0].df and the current device time is <= 180 days, sorted descending by r[0].df
 * i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule4findRecentRecovery(
    nowUtc: Instant
): CwaCovidCertificate? = this
    .filterIsInstance<RecoveryCertificate>()
    .filter {
        Days.daysBetween(it.rawCertificate.recovery.validFrom, nowUtc.toLocalDateUtc()).days <= 180
    }.maxByOrNull { it.rawCertificate.recovery.validFrom }

/**
 * 5
 * Series-completing Vaccination Certificate <= 14 days
 * Find Vaccination Certificates (i.e. DGC with v[0]) where v[0].dn equal to v[0].sd and the time difference
 * between the time represented by v[0].dt and the current device time is <= 14 days,
 * sorted descending by v[0].dt (i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule5findTooRecentFinalShot(
    nowUtc: Instant
): CwaCovidCertificate? = this
    .filterIsInstance<VaccinationCertificate>()
    .filter {
        with(it.rawCertificate.vaccination) { doseNumber == totalSeriesOfDoses }
    }
    .filter {
        Days.daysBetween(it.rawCertificate.vaccination.vaccinatedOn, nowUtc.toLocalDateUtc()).days <= 14
    }
    .maxWithOrNull(
        compareBy(
            { it.rawCertificate.vaccination.vaccinatedOn },
            { it.headerIssuedAt }
        )
    )

/**
 * 6
 * Other Vaccination Certificate
 * Find Vaccination Certificates (i.e. DGC with v[0])sorted descending by v[0].dt (i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule6findOtherVaccinations(): CwaCovidCertificate? = this
    .filterIsInstance<VaccinationCertificate>()
    .maxWithOrNull(
        compareBy(
            { it.rawCertificate.vaccination.vaccinatedOn },
            { it.headerIssuedAt }
        )
    )

/**
 * 7
 * Recovery Certificate > 180 days
 * Find Recovery Certificates (i.e. DGC with r[0]) where the time difference between the time represented by r[0].df
 * and the current device time is > 180 days, sorted descending by r[0].df (i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule7FindOldRecovery(
    nowUtc: Instant
): CwaCovidCertificate? = this
    .filterIsInstance<RecoveryCertificate>()
    .filter {
        Days.daysBetween(it.rawCertificate.recovery.validFrom, nowUtc.toLocalDateUtc()).days > 180
    }
    .maxByOrNull { it.rawCertificate.recovery.validFrom }

/**
 * 8
 * PCR Test Certificate > 72 hours
 * Find Test Certificates (i.e. DGC with t[0]) where t[0].tt is set to LP6464-4 and the time difference between
 * the time represented by t[0].sc and the current device time is > 72 hours,
 * sorted descending by t[0].sc (i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule8FindOldPcrTest(
    nowUtc: Instant
): CwaCovidCertificate? = this
    .filterIsInstance<TestCertificate>()
    .filter { it.rawCertificate.test.testType == "LP6464-4" }
    .filter { Duration(it.rawCertificate.test.sampleCollectedAt, nowUtc) > Duration.standardHours(72) }
    .maxByOrNull { it.rawCertificate.test.sampleCollectedAt }

/**
 * 9
 * RAT Test Certificate > 48 hours
 * Find Test Certificates (i.e. DGC with t[0]) where t[0].tt is set to LP217198-3 and the time difference between
 * the time represented by t[0].sc and the current device time is > 48 hours,
 * sorted descending by t[0].sc (i.e. latest first).
 * If there is one or more certificates matching these requirements,
 * the first one is returned as a result of the operation.
 */
private fun Collection<CwaCovidCertificate>.rule9FindOldRaTest(
    nowUtc: Instant
): CwaCovidCertificate? = this
    .filterIsInstance<TestCertificate>()
    .filter { it.rawCertificate.test.testType == "LP217198-3" }
    .filter { Duration(it.rawCertificate.test.sampleCollectedAt, nowUtc) > Duration.standardHours(48) }
    .maxByOrNull { it.rawCertificate.test.sampleCollectedAt }

@Suppress("ReturnCount", "ComplexMethod")
fun Collection<CwaCovidCertificate>.findHighestPriorityCertificate(
    nowUtc: Instant = Instant.now()
): CwaCovidCertificate? = this
    .also { Timber.v("findHighestPriorityCertificate(nowUtc=%s): %s", nowUtc, this) }
    .run {
        val valid = mutableListOf<CwaCovidCertificate>()
        val expired = mutableListOf<CwaCovidCertificate>()
        val invalid = mutableListOf<CwaCovidCertificate>()

        this.forEach {
            when (it.getState()) {
                is CwaCovidCertificate.State.Valid,
                is CwaCovidCertificate.State.ExpiringSoon -> valid.add(it)
                is CwaCovidCertificate.State.Expired -> expired.add(it)
                is CwaCovidCertificate.State.Invalid -> invalid.add(it)
            }
        }

        listOf(valid to "Valid/ExpiringSoon", expired to "Expired", invalid to "Invalid")
    }
    .mapNotNull { (certsForState, stateName) ->
        // Correct rulefinding depends on the explicit list ordering generated in the previous step
        // list(list(valid+expiring_soon), list(expired), list(invalid))

        if (certsForState.isEmpty()) {
            Timber.v("No certs with state %s", stateName)
            return@mapNotNull null
        } else {
            Timber.v("Checking %d certs with for %s", certsForState.size, stateName)
        }

        certsForState.rule1FindRecentPcrCertificate(nowUtc)?.let {
            Timber.d("Rule 1 match (PCR Test Certificate <= 72 hours): %s", it)
            return@mapNotNull it
        }

        certsForState.rule2FindRecentRaCertificate(nowUtc)?.let {
            Timber.d("Rule 2 match (RA Test Certificate <= 48 hours): %s", it)
            return@mapNotNull it
        }

        certsForState.rule3FindRecentLastShot(nowUtc)?.let {
            Timber.d(
                "Rule 3 match (Vaccination Certificate with full dose that are either booster or > 14 days): %s",
                it
            )
            return@mapNotNull it
        }

        certsForState.rule4findRecentRecovery(nowUtc)?.let {
            Timber.d("Rule 4 match (Recovery Certificate <= 180 days): %s", it)
            return@mapNotNull it
        }

        certsForState.rule5findTooRecentFinalShot(nowUtc)?.let {
            Timber.d("Rule 5 match (Vaccination Certificate with full dose <= 14 days): %s", it)
            return@mapNotNull it
        }

        certsForState.rule6findOtherVaccinations()?.let {
            Timber.d("Rule 6 match (Other Vaccination Certificate): %s", it)
            return@mapNotNull it
        }

        certsForState.rule7FindOldRecovery(nowUtc)?.let {
            Timber.d("Rule 7 match (Recovery Certificate > 180 days): %s", it)
            return@mapNotNull it
        }

        certsForState.rule8FindOldPcrTest(nowUtc)?.let {
            Timber.d("Rule 8 match (PCR Test Certificate > 72 hours): %s", it)
            return@mapNotNull it
        }

        certsForState.rule9FindOldRaTest(nowUtc)?.let {
            Timber.d("Rule 9 match (RAT Test Certificate > 48 hours): %s", it)
            return@mapNotNull it
        }

        Timber.v("No rule matched for state %s", stateName)
        null
    }
    .firstOrNull()
    ?: firstOrNull().also {
        /**
         * Fallback: return the first DGC from the set.
         * Note that this fallback should never apply in a real scenario.
         */
        Timber.e("No priority match, this should not happen: %s", this)
    }

fun Collection<CwaCovidCertificate>.determineAdmissionState(nowUtc: Instant = Instant.now()): PersonCertificates.AdmissionState? {

    Timber.v("Determining the admission state(nowUtc=%s): %s", nowUtc, this)

    if (isEmpty()) {
        Timber.v("Admission state cannot be determined, there are no certificates")
        return null
    }

    // The operations from the tech spec are documented as comments here

    // 1. validity state has to be VALID or EXPIRING_SOON
    // => we are only passing valid certificates to this function

    // 2. determine has2G: at least one valid vaccination or recovery certificate
    val recentVaccination = rule3FindRecentLastShot(nowUtc)
    val recentRecovery = rule4findRecentRecovery(nowUtc)

    val hasVaccination = recentVaccination != null
    val hasRecentRecovery = recentRecovery != null

    val has2G = hasVaccination || hasRecentRecovery

    // 3. determine hasPCR and 4. hasRAT
    val recentPCR = rule1FindRecentPcrCertificate(nowUtc)
    val recentRAT = rule2FindRecentRaCertificate(nowUtc)

    val hasPCR = recentPCR != null
    val hasRAT = recentRAT != null

    // 5. determine admission state
    when {
        has2G -> {
            val twoGCertificate = recentVaccination ?: recentRecovery!!
            if (hasPCR) {
                Timber.v("Determined admission state = 2G+ PCR")
                return TwoGPlusPCR(twoGCertificate, recentPCR!!)
            } else if (hasRAT) {
                Timber.v("Determined admission state = 2G+ RAT")
                return TwoGPlusRAT(twoGCertificate, recentRAT!!)
            }
            Timber.v("Determined admission state = 2G")
            return TwoG(twoGCertificate)
        }
        hasPCR -> {
            Timber.v("Determined admission state = 3G with PCR")
            return ThreeGWithPCR(recentPCR!!)
        }
        hasRAT -> {
            Timber.v("Determined admission state = 3G with RAT")
            return ThreeGWithRAT(recentRAT!!)
        }
        else -> {
            Timber.v("Determined admission state = other")
            return when (val certificate = findHighestPriorityCertificate(nowUtc)) {
                null -> null
                else -> Other(certificate)
            }
        }
    }
}
