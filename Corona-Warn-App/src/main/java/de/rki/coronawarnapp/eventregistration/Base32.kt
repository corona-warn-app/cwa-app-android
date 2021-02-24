package de.rki.coronawarnapp.eventregistration

import com.google.common.io.BaseEncoding

/**
 * Decodes input String into byte array using Base32 decoder
 * @return [ByteArray]
 */
fun String.decodeBase32ToArray(): ByteArray = BaseEncoding.base32().decode(this)

/**
 * Decodes input String into Base32 decodes String
 * @return [String]
 */
fun String.decodeBase32ToString(): String = String(decodeBase32ToArray())

/**
 * Returns Base32 encoded string using [Charsets.UTF_8]
 * @param padding [Boolean] true by default ,Output will have '=' characters padding
 * @return [String]
 */
fun String.encodeBase32ToString(padding: Boolean = true): String = when {
    padding -> BaseEncoding.base32().encode(toByteArray(Charsets.UTF_8))
    else -> BaseEncoding.base32().omitPadding().encode(toByteArray(Charsets.UTF_8))
}
