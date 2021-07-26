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
        val diff = daysUntilExpiration - expirationThreshold.standardDays
        return when {
            daysUntilExpiration == 0 -> {
                if (now.toDateTime(timeZone).isAfter(expiresAt)) {
                    CwaCovidCertificate.State.Expired(expiresAt)
                } else {
                    CwaCovidCertificate.State.ExpiringSoon(expiresAt)
                }
            }
            daysUntilExpiration < 0 -> CwaCovidCertificate.State.Expired(expiresAt)
            diff > 0 -> CwaCovidCertificate.State.Valid(expiresAt)
            diff <= 0 -> CwaCovidCertificate.State.ExpiringSoon(expiresAt)
            else -> throw IllegalArgumentException() // impossible!
        }
    }
}
