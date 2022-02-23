package de.rki.coronawarnapp.dccreissuance.core.server.validation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.dccreissuance.core.common.DccReissuanceException
import de.rki.coronawarnapp.tag
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.security.cert.Certificate
import javax.inject.Inject

class DccReissuanceServerCertificateValidator @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    /**
     *
     * @throws [DccReissuanceException] if the check fails
     *
     * Note that the absence of an error code indicates a successful check
     */
    suspend fun validate(certificateChain: List<Certificate>) = try {
        val leafCertificate = certificateChain.first()
        //TODO: Add reissueServicePublicKeyDigest to app config
        val reissueServicePublicKeyDigest = appConfigProvider.getAppConfig()

        val publicKeyHash256 = leafCertificate.publicKey.encoded.toByteString().sha256()

        if (publicKeyHash256 != reissueServicePublicKeyDigest) throw DccReissuanceException(
            errorCode = DccReissuanceException.ErrorCode.DCC_RI_PIN_MISMATCH
        )

        Timber.tag(TAG).d("Certificate check was successful")
    } catch (e: Exception) {
        throw when (e) {
            is DccReissuanceException -> e
            else -> {
                Timber.tag(TAG).w(
                    e,
                    "Certificate validation failed with an unspecified error. Needs further investigation!"
                )
                DccReissuanceException(errorCode = DccReissuanceException.ErrorCode.DCC_RI_PIN_MISMATCH, cause = e)
            }
        }
    }

    companion object {
        private val TAG = tag<DccReissuanceServerCertificateValidator>()
    }
}
