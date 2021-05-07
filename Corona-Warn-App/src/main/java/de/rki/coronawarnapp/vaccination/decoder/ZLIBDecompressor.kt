package de.rki.coronawarnapp.vaccination.decoder

import timber.log.Timber
import java.util.zip.InflaterInputStream
import javax.inject.Inject

class ZLIBDecompressor @Inject constructor() {

    fun decode(input: ByteArray): ByteArray {
        if (input.size >= 2 && input[0] == 0x78.toByte()) {
            // ZLIB magic headers
            if (input[1] == 0x01.toByte() ||
                input[1] == 0x5E.toByte() ||
                input[1] == 0x9C.toByte() ||
                input[1] == 0xDA.toByte()
            ) {
                return try {
                    InflaterInputStream(input.inputStream()).readBytes()
                } catch (e: Throwable) {
                    Timber.e(e)
                    throw InvalidInputException("Zlib decompression failed.")
                }
            }
        }
        return input
    }
}
