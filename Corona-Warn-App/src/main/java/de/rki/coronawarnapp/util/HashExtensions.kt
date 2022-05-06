package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.util.HashExtensions.Format.BASE64
import de.rki.coronawarnapp.util.HashExtensions.Format.HEX
import okio.ByteString.Companion.toByteString
import java.io.File
import java.security.MessageDigest

internal object HashExtensions {

    enum class Format {
        HEX, BASE64
    }

    fun ByteArray.toSHA256(format: Format = HEX) = this.hashByteArray("SHA-256", format)

    fun ByteArray.toSHA1(format: Format = HEX) = this.hashByteArray("SHA-1", format)

    fun ByteArray.toMD5(format: Format = HEX) = this.hashByteArray("MD5", format)

    fun ByteArray.toHexString() = this.formatHash(HEX)

    fun String.toSHA256(format: Format = HEX) = this.hashString("SHA-256", format)

    fun String.toSHA1(format: Format = HEX) = this.hashString("SHA-1", format)

    fun String.toMD5(format: Format = HEX) = this.hashString("MD5", format)

    fun String.sha256() = toByteArray().toByteString().sha256()

    private fun String.hashString(type: String, format: Format): String = toByteArray().hashByteArray(type, format)

    private fun ByteArray.hashByteArray(type: String, format: Format): String = MessageDigest
        .getInstance(type)
        .digest(this)
        .formatHash(format)

    private fun ByteArray.formatHash(format: Format): String = when (format) {
        HEX -> this.joinToString(separator = "") { String.format("%02X", it) }.lowercase()
        BASE64 -> this.toByteString().base64()
    }

    fun File.hashToMD5(format: Format = HEX): String = this.hashTo("MD5", format)

    private fun File.hashTo(type: String, format: Format): String = MessageDigest
        .getInstance(type)
        .let { md ->
            inputStream().use { stream ->
                val buffer = ByteArray(8192)
                var read: Int
                while (stream.read(buffer).also { read = it } > 0) {
                    md.update(buffer, 0, read)
                }
            }
            md.digest()
        }
        .formatHash(format)
}
