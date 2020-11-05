package de.rki.coronawarnapp.util

import java.io.File
import java.security.MessageDigest
import java.util.Locale

internal object HashExtensions {

    fun ByteArray.toSHA256() = this.hashByteArray("SHA-256")

    fun ByteArray.toSHA1() = this.hashByteArray("SHA-1")

    fun ByteArray.toMD5() = this.hashByteArray("MD5")

    fun String.toSHA256() = this.hashString("SHA-256")

    fun String.toSHA1() = this.hashString("SHA-1")

    fun String.toMD5() = this.hashString("MD5")

    private fun String.hashString(type: String): String = toByteArray().hashByteArray(type)

    private fun ByteArray.hashByteArray(type: String): String = MessageDigest
        .getInstance(type)
        .digest(this)
        .formatHash()

    private fun ByteArray.formatHash(): String = this
        .joinToString(separator = "") { String.format("%02X", it) }
        .toLowerCase(Locale.ROOT)

    fun File.hashToMD5(): String = this.hashTo("MD5")

    private fun File.hashTo(type: String): String = MessageDigest
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
        .formatHash()
}
