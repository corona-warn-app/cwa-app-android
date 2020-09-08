package de.rki.coronawarnapp.util

import java.io.File
import java.security.MessageDigest
import java.util.Locale

internal object HashExtensions {

    fun String.toSHA256() = this.hashString("SHA-256")

    fun String.toSHA1() = this.hashString("SHA-1")

    fun String.toMD5() = this.hashString("MD5")

    private fun ByteArray.formatHash(): String = this
        .joinToString(separator = "") { String.format("%02X", it) }
        .toLowerCase(Locale.ROOT)

    private fun String.hashString(type: String): String = MessageDigest
        .getInstance(type)
        .digest(this.toByteArray())
        .formatHash()

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
