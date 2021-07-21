/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 *
 * Modifications Copyright (c) 2021 SAP SE or an SAP affiliate company.
 */

package de.rki.coronawarnapp.covidcertificate.signature.core.utils

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import java.math.BigInteger
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException

/**
 * Based on
 * https://github.com/Digitaler-Impfnachweis/covpass-android/blob/5f0e0bec58e948861ecb92a78d74e553c639eb9c/covpass-sdk/src/main/java/de/rki/covpass/sdk/crypto/SignatureUtils.kt
 */

/**
 * Validates the given [signature].
 *
 * @throws SignatureException if validation fails.
 */
fun validateSignature(key: PublicKey, data: ByteArray, signature: ByteArray, algorithm: String) {
    val javaSignature = if (algorithm.endsWith("withECDSA")) {
        val (r, s) = signature.splitHalves()
        DERSequence(
            arrayOf(
                ASN1Integer(BigInteger(1, r)),
                ASN1Integer(BigInteger(1, s)),
            )
        ).encoded
    } else {
        signature
    }
    val verifier = Signature.getInstance(algorithm).apply {
        initVerify(key)
        update(data)
    }
    if (!verifier.verify(javaSignature)) {
        throw SignatureException()
    }
}

private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> =
    take(size / 2).toByteArray() to drop(size / 2).toByteArray()
