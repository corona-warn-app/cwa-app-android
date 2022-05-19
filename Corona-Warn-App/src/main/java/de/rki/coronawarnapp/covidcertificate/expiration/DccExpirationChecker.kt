package de.rki.coronawarnapp.covidcertificate.expiration

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysUntil
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DccExpirationChecker @Inject constructor() {

    fun getExpirationState(
        dccData: DccData<*>,
        expirationThreshold: Duration,
        now: Instant,
        timeZone: DateTimeZone = DateTimeZone.getDefault()
    ): CwaCovidCertificate.State = with(dccData) {
        val expiresAt = header.expiresAt
        val daysUntilExpiration = now.daysUntil(expiresAt, timeZone)

        return when {
            daysUntilExpiration < 0 -> CwaCovidCertificate.State.Expired(expiresAt)
            daysUntilExpiration == 0 -> {
                if (now.isAfter(expiresAt)) {
                    CwaCovidCertificate.State.Expired(expiresAt)
                } else {
                    CwaCovidCertificate.State.ExpiringSoon(expiresAt)
                }
            }
            daysUntilExpiration <= expirationThreshold.standardDays -> CwaCovidCertificate.State.ExpiringSoon(expiresAt)
            daysUntilExpiration > expirationThreshold.standardDays -> CwaCovidCertificate.State.Valid(expiresAt)
            else -> throw IllegalArgumentException() // impossible!
        }
    }
}

fun DccData<*>.isExpired(
    now: Instant,
): Boolean = header.expiresAt < now
