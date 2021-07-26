package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DscSignatureValidator @Inject constructor() {

    suspend fun isSignatureValid(dscData: DscData, certificateData: DccData<*>) {
        Timber.tag(TAG).d("isSignatureValid(certificateData=%s)", certificateData.header)
        // TODO
    }

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
