package de.rki.coronawarnapp.dccreissuance.core.server.validation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.tag
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.security.cert.Certificate
import javax.inject.Inject

class DccReissuanceServerCertificateValidator @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    /**
     * Checks the SHA-256 hash of the public key of the leaf certificate (first certificate in the chain) and compares
     * it against the hash from App Config parameter [CovidCertificateConfig.reissueServicePublicKeyDigest].
     *
     * Note that the absence of an error code indicates a successful check
     *
     * @throws [DccReissuanceException] if the hashes do not match
     */
    suspend fun checkCertificateChain(certificateChain: List<Certificate>) = try {
        Timber.tag(TAG).d("Check Certificate Chain")
        val leafCertificate = certificateChain.first()
        val reissueServicePublicKeyDigest = appConfigProvider.getAppConfig()
            .covidCertificateParameters
            .reissueServicePublicKeyDigest

        val publicKeyHash256 = leafCertificate.publicKey.encoded.toByteString().sha256()

        Timber.tag(TAG).d(
            "Comparing publicKeyHash256=%s and reissueServicePublicKeyDigest=%s",
            publicKeyHash256,
            reissueServicePublicKeyDigest
        )

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
