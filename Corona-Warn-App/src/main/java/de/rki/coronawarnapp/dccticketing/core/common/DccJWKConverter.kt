package de.rki.coronawarnapp.dccticketing.core.common

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.check.createSha256Fingerprint
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import okhttp3.CertificatePinner
import okio.ByteString.Companion.decodeBase64
import java.net.URL
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject

@Reusable
class DccJWKConverter @Inject constructor() {

    private val x509CertificateFactory: CertificateFactory by lazy {
        CertificateFactory.getInstance("X.509")
    }

    /**
     * Takes the first element of [DccJWK.x5c] and converts in into a [X509Certificate]
     *
     * @param jwk [DccJWK]
     * @return [X509Certificate]
     */
    fun createX509Certificate(jwk: DccJWK): X509Certificate = jwk.x5c
        .first()
        .decodeBase64()
        ?.toByteArray()
        ?.inputStream()
        .use {
            x509CertificateFactory.generateCertificate(it) as X509Certificate
        }

    /**
     * Takes the first element of [DccJWK.x5c] and converts in into a [PublicKey]
     *
     * @param jwk [DccJWK]
     * @return [PublicKey]
     */
    fun createPublicKey(jwk: DccJWK): PublicKey = createX509Certificate(jwk).publicKey

    fun getCertificatePinner(
        url: String,
        jwkSet: Set<DccJWK>
    ): CertificatePinner {
        val requiredCertificates = jwkSet.map { createX509Certificate(jwk = it) }
        return CertificatePinner.Builder().apply {
            val keys = requiredCertificates.map {
                "sha256/${it.createSha256Fingerprint()}"
            }.toTypedArray()
            add(URL(url).host, *keys)
        }.build()
    }
}
