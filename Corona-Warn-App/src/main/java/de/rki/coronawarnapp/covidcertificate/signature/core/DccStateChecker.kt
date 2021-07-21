package de.rki.coronawarnapp.covidcertificate.signature.core

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DccStateChecker @Inject constructor(
    private val timeStamper: TimeStamper,
    private val dscRepository: DscRepository,
    private val appConfigProvider: AppConfigProvider,
    private val dscSignatureValidator: DscSignatureValidator,
) {

    suspend fun checkState(
        dccData: DccData<*>
    ): Flow<CwaCovidCertificate.State> = flow {
        // TODO signature check

        // expiration check
        val expirationThresholdInDays = appConfigProvider.currentConfig.first()
            .covidCertificateParameters.expirationThresholdInDays
        val state = dccData.getExpirationState(expirationThresholdInDays, timeStamper.nowUTC)
        emit(state)
    }
}

@VisibleForTesting
internal fun DccData<*>.getExpirationState(
    expirationThresholdInDays: Int,
    now: Instant,
    timeZone: DateTimeZone = DateTimeZone.getDefault()
): CwaCovidCertificate.State {
    val expiresAt = header.expiresAt
    val daysUntilExpiration = expiresAt.daysUntil(now, timeZone)
    val diff = daysUntilExpiration - expirationThresholdInDays
    return when {
        daysUntilExpiration <= 0 -> CwaCovidCertificate.State.Expired(expiresAt)
        diff > 0 -> CwaCovidCertificate.State.Valid(expiresAt)
        diff <= 0 -> CwaCovidCertificate.State.ExpiringSoon(expiresAt)
        else -> throw IllegalArgumentException() // impossible!
    }
}

private fun Instant.daysUntil(
    now: Instant,
    timeZone: DateTimeZone = DateTimeZone.getDefault()
): Int {
    val expirationDate = toDateTime(timeZone).toLocalDate()
    val today = now.toDateTime(timeZone).toLocalDate()
    return Days.daysBetween(today, expirationDate).days
}
