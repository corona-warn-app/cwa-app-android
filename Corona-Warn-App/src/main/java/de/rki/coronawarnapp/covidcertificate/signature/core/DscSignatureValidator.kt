package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DscSignatureValidator @Inject constructor(
    private val dscRepository: DscRepository,
) {

    suspend fun isSignatureValid(data: DccData<*>): Boolean {
        Timber.tag(TAG).d("isSignatureValid(data=%s)", data.header)
        // TODO
        return true
    }

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
