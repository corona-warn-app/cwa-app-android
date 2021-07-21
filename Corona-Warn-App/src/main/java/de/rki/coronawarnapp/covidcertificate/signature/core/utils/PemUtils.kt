/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 *
 * Modifications Copyright (c) 2021 SAP SE or an SAP affiliate company.
 */

package de.rki.coronawarnapp.covidcertificate.signature.core.utils

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.security.PublicKey
import java.security.cert.X509Certificate

/**
 * Based on
 * https://github.com/Digitaler-Impfnachweis/covpass-android/blob/5f0e0bec58e948861ecb92a78d74e553c639eb9c/covpass-sdk/src/main/java/de/rki/covpass/sdk/crypto/PemUtils.kt
 */

/** Reads a PEM file and returns a list of the [X509Certificate]s contained in that file. */
fun readPem(data: String): List<X509Certificate> {
    val converter = JcaX509CertificateConverter()
    return readRawPem(data)
        .mapNotNull { it as? X509CertificateHolder }
        .map { converter.getCertificate(it) }
        .toList()
}

/** Reads a PEM file and returns a list of the [PublicKey]s contained in that file. */
fun readPemKeys(data: String): List<PublicKey> {
    val converter = JcaPEMKeyConverter()
    return readRawPem(data)
        .mapNotNull { SubjectPublicKeyInfo.getInstance(it) }
        .map { converter.getPublicKey(it) }
        .toList()
}

/** Returns the raw objects contained in the PEM file. */
fun readRawPem(data: String): Sequence<Any> = sequence {
    val parser = PEMParser(normalizePem(data).reader())
    while (true) {
        val item = parser.readObject()
            ?: break
        yield(item)
    }
}

/** This removes comments and strips empty lines from the PEM string. */
private fun normalizePem(data: String): String =
    data.replace(commentRegex, "").replace(newlineRegex, "\n").trim()

private val commentRegex = Regex("#[^\n]*")
private val newlineRegex = Regex("\n\n+")
