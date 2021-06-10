package de.rki.coronawarnapp.util.encoding

import com.google.common.io.BaseEncoding
import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * Decodes [String] into [ByteString] using Base32 decoder
 * @return [ByteString]
 */
fun String.decodeBase32(): ByteString = BaseEncoding.base32().decode(this).toByteString()

/**
 * Encodes [ByteString] into base32 [String]
 * @return [String]
 */
fun ByteString.base32(padding: Boolean = true): String = when {
    padding -> BaseEncoding.base32().encode(toByteArray())
    else -> BaseEncoding.base32().omitPadding().encode(toByteArray())
}

/**
 * Returns Base32 encoded string using [Charsets.UTF_8]
 * @param padding [Boolean] true by default ,Output will have '=' characters padding
 * @return [String]
 */
fun String.base32(padding: Boolean = true): String = when {
    padding -> BaseEncoding.base32().encode(toByteArray(Charsets.UTF_8))
    else -> BaseEncoding.base32().omitPadding().encode(toByteArray(Charsets.UTF_8))
}
