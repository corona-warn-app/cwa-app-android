package de.rki.coronawarnapp.covidcertificate.common.statecheck

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationChecker
import de.rki.coronawarnapp.covidcertificate.revocation.check.DccRevocationChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccStateChecker @Inject constructor(
    private val timeStamper: TimeStamper,
    private val appConfigProvider: AppConfigProvider,
    private val dscSignatureValidator: DscSignatureValidator,
    private val dccExpirationChecker: DccExpirationChecker,
    private val dccRevocationChecker: DccRevocationChecker,
) {

    suspend operator fun invoke(
        dccData: DccData<*>,
        qrCodeHash: String,
        dccStateValidity: DccStateValidity
    ): CwaCovidCertificate.State = when {
        dccRevocationChecker.isRevoked(dccData, dccStateValidity.revocationList) -> Revoked
        qrCodeHash in dccStateValidity.blockedQrCodeHashes -> Blocked
        else -> runCatching {
            val threshold = appConfigProvider.currentConfig.first().covidCertificateParameters.expirationThreshold
            dscSignatureValidator.validateSignature(dccData, dccStateValidity.dscSignatureList) // throws if invalid
            dccExpirationChecker.getExpirationState(dccData, threshold, timeStamper.nowUTC)
        }.getOrElse {
            Timber.tag(TAG).w("Certificate had invalid signature %s", it.message)
            Invalid()
        }
    }

    companion object {
        private val TAG = tag<DccStateChecker>()
    }
}
