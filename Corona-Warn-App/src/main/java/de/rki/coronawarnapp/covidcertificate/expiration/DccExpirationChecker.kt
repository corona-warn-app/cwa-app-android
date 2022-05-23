package de.rki.coronawarnapp.covidcertificate.expiration

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import org.joda.time.Duration
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DccExpirationChecker @Inject constructor() {

    fun getExpirationState(
        dccData: DccData<*>,
        expirationThreshold: Duration,
        now: Instant
    ): CwaCovidCertificate.State = with(dccData) {
        val expiresAt = header.expiresAt
        val timeDiffUntilExpiration = Duration(now, expiresAt)

        return when {
            expiresAt <= now -> CwaCovidCertificate.State.Expired(expiresAt)
            timeDiffUntilExpiration <= expirationThreshold -> CwaCovidCertificate.State.ExpiringSoon(expiresAt)
            else -> CwaCovidCertificate.State.Valid(expiresAt)
        }
    }
}

fun DccData<*>.isExpired(
    now: Instant,
): Boolean = header.expiresAt < now
