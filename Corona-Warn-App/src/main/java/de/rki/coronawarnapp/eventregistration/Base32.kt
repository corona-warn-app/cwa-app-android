package de.rki.coronawarnapp.eventregistration

import com.google.common.io.BaseEncoding
import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * Decodes String into [ByteArray] using Base32 decoder
 * @return [ByteArray]
 */
fun String.decodeBase32ToArray(): ByteArray = BaseEncoding.base32().decode(this)

/**
 * Decodes String into [ByteString] using Base32 decoder
 * @return [ByteString]
 */
fun String.decodeBase32ToByteString(): ByteString = decodeBase32ToArray().toByteString()

/**
 * Decodes String into Base32 decoded String
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
