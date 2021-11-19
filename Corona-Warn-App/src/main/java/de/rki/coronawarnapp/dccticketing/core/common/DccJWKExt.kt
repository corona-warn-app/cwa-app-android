package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import okio.ByteString.Companion.decodeBase64
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

fun DccJWK.getPublicKey(certificateFactory: CertificateFactory) = x5c.first()
    .decodeBase64()
    ?.toByteArray()
    ?.inputStream()
    .use {
        certificateFactory.generateCertificate(it) as X509Certificate
    }.publicKey
