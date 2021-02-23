package de.rki.coronawarnapp.eventregistration

import com.google.common.io.BaseEncoding

/**
 * Base32 Encoding wrapper around [BaseEncoding.base32]
 */
class Base32Encoder {
    private val encoder = BaseEncoding.base32()

    /**
     * Decodes input String into byte array using Base32 decoder
     * @param input [String]
     * @return [ByteArray]
     */
    fun decode(input: String): ByteArray = encoder.decode(input)

    /**
     * Decodes input String into Base32 decodes String
     * @param input [String]
     * @return [String]
     */
    fun decodeToString(input: String): String = String(decode(input))

    /**
     * Returns Base32 encoded string using [Charsets.UTF_8]
     * @param input [String]
     * @param padding [Boolean] true by default ,Output will have '=' characters padding
     * @return [String]
     */
    fun encode(input: String, padding: Boolean = true): String = when {
        padding -> encoder.encode(input.toByteArray(Charsets.UTF_8))
        else -> encoder.omitPadding().encode(input.toByteArray(Charsets.UTF_8))
    }
}
