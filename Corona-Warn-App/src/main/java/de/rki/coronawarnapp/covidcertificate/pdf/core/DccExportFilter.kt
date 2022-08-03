package de.rki.coronawarnapp.covidcertificate.pdf.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toInstantMidnightUtc
import de.rki.coronawarnapp.util.toJavaInstant
import de.rki.coronawarnapp.util.toJavaTime
import java.time.Duration
import java.time.Instant

internal fun Collection<CwaCovidCertificate>.filterAndSortForExport(
    nowUtc: Instant
): List<CwaCovidCertificate> {
    return filter {
        it.isIncludedInExport(nowUtc)
    }.sort()
}

internal fun CwaCovidCertificate.isIncludedInExport(nowUtc: Instant): Boolean {
    return state.isIncludedInExport && when (this) {
        is TestCertificate -> this.isRecent(nowUtc)
        else -> true
    }
}

internal fun List<CwaCovidCertificate>.sort(): List<CwaCovidCertificate> = sortedWith(
    compareBy(
        { it.fullName },
        {
            when (it) {
                is TestCertificate -> it.sampleCollectedAt
                is VaccinationCertificate -> it.vaccinatedOn?.toInstantMidnightUtc()
                is RecoveryCertificate -> it.testedPositiveOn?.toInstantMidnightUtc()
                else -> null
            }
        }
    )
)

internal fun TestCertificate.isRecent(nowUtc: Instant): Boolean {
    return this.sampleCollectedAt?.let {
        Duration.between(it.toJavaInstant(), nowUtc) <= Duration.ofHours(72)
    } ?: false
}

internal val CwaCovidCertificate.State.isIncludedInExport: Boolean
    get() = this is CwaCovidCertificate.State.Valid ||
        this is CwaCovidCertificate.State.Expired ||
        this is CwaCovidCertificate.State.ExpiringSoon ||
        this is CwaCovidCertificate.State.Invalid
