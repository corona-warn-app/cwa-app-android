package de.rki.coronawarnapp.util.compression

import okio.ByteString
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.util.zip.InflaterInputStream
import javax.inject.Inject

class ZLIBCompression @Inject constructor() {
    fun decompress(input: ByteString): ByteString = if (
        input.size >= 2 &&
        input[0] == 0x78.toByte() &&
        input[1] in listOf(0x01.toByte(), 0x5E.toByte(), 0x9C.toByte(), 0xDA.toByte())
    ) {
        try {
            input.toByteArray().inputStream().use { InflaterInputStream(it).readBytes().toByteString() }
        } catch (e: Throwable) {
            Timber.e(e)
            throw InvalidInputException("Zlib decompression failed.")
        }
    } else {
        input
    }
}
