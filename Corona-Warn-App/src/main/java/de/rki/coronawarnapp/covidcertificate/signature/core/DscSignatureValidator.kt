package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.signature.core.exception.DscSignatureValidationException
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DscSignatureValidator @Inject constructor() {

    /**
     *
     * @throws DscSignatureValidationException
     */
    suspend fun isSignatureValid(dscData: DscData, certificateData: DccData<*>): Boolean {
        Timber.tag(TAG).d("isSignatureValid(dscData=%s,certificateData=%s)", dscData, certificateData)
        // TODO
        return true
    }

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
