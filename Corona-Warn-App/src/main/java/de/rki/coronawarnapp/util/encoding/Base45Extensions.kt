package de.rki.coronawarnapp.util.encoding

/**
 * Decodes [String] into [ByteArray] using Base45 decoder
 * @return [ByteArray]
 */
fun String.decodeBase45(): ByteArray = Base45Decoder.decode(this)

/**
 * Encodes [ByteArray] into base45 [String]
 * @return [String]
 */
fun ByteArray.base45(): String = Base45Decoder.encode(this)
