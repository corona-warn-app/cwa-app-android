package de.rki.coronawarnapp.util.encoding

import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * Decodes [String] into [ByteString] using Base45 decoder
 * @return [ByteString]
 */
fun String.decodeBase45(): ByteString = Base45Decoder.decode(this).toByteString()

/**
 * Encodes [ByteString] into base45 [String]
 * @return [String]
 */
fun ByteString.base45(): String = Base45Decoder.encode(this.toByteArray())
