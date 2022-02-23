package de.rki.coronawarnapp.dccreissuance.core.server.validation

import de.rki.coronawarnapp.dccreissuance.core.common.DccReissuanceException
import de.rki.coronawarnapp.tag
import timber.log.Timber
import java.security.cert.Certificate
import javax.inject.Inject

class DccReissuanceServerCertificateValidator @Inject constructor() {

    fun validate(certificateChain: List<Certificate>) = try {

    } catch (e: Exception) {
        Timber.tag(TAG).w(e, "Failed to validate Dcc Reissuance Server Certificate")
        throw DccReissuanceException(errorCode = DccReissuanceException.ErrorCode.DCC_RI_PIN_MISMATCH, cause = e)
    }

    companion object {
        private val TAG = tag<DccReissuanceServerCertificateValidator>()
    }
}
