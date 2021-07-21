package de.rki.coronawarnapp.covidcertificate.signature.core

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

        val expirationThresholdInDays = appConfigProvider.currentConfig.first()
            .covidCertificateParameters.expirationThresholdInDays
        val daysUntilExpiration = dccData.daysUntilExpiration(timeStamper.nowUTC)
        val diff = daysUntilExpiration - expirationThresholdInDays
        val expiresAt = dccData.header.expiresAt
        val state: CwaCovidCertificate.State = when {
            daysUntilExpiration <= 0 -> CwaCovidCertificate.State.Expired(expiresAt)
            diff > 0 -> CwaCovidCertificate.State.Valid(expiresAt)
            diff <= 0 -> CwaCovidCertificate.State.ExpiringSoon(expiresAt)
            else -> throw IllegalArgumentException()// impossible!
        }
        emit(state)
    }

    private fun DccData<*>.daysUntilExpiration(
        now: Instant,
        timeZone: DateTimeZone = DateTimeZone.getDefault()
    ): Int {
        val expirationDate = header.expiresAt.toDateTime(timeZone).toLocalDate()
        val today = now.toDateTime(timeZone).toLocalDate()
        return Days.daysBetween(today, expirationDate).days
    }
}
