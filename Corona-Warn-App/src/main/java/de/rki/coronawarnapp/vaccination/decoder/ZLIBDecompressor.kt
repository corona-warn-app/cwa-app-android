package de.rki.coronawarnapp.vaccination.decoder

import timber.log.Timber
import java.util.zip.InflaterInputStream
import javax.inject.Inject

class ZLIBDecompressor @Inject constructor() {

    fun decompress(input: ByteArray): ByteArray {
        if (input.size >= 2 && input[0] == 0x78.toByte()) {
            // ZLIB magic headers
            if (input[1] in listOf(0x01.toByte(), 0x5E.toByte(), 0x9C.toByte(), 0xDA.toByte())) {
                return try {
                    input.inputStream().use { InflaterInputStream(it).readBytes() }
                } catch (e: Throwable) {
                    Timber.e(e)
                    throw InvalidInputException("Zlib decompression failed.")
                }
            }
        }
        return input
    }
}
