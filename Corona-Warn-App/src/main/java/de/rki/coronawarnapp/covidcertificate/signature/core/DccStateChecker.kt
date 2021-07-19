package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.joda.time.Duration
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
        // TODO
        val state = CwaCovidCertificate.State.Valid(
            expiresAt = Instant.now().plus(Duration.standardDays(21))
        )
        emit(state)
    }
}
