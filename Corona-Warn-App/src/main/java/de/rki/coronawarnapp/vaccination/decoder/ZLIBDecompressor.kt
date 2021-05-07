package de.rki.coronawarnapp.vaccination.decoder

import timber.log.Timber
import java.util.zip.InflaterInputStream
import javax.inject.Inject

// licence

class ZLIBDecompressor @Inject constructor() {

    fun decode(input: ByteArray): ByteArray {
        if (input.size >= 2 && input[0] == 0x78.toByte()) {
            // ZLIB magic headers
            if (input[1] == 0x01.toByte() || // Level 1
                input[1] == 0x5E.toByte() || // Level 2 - 5
                input[1] == 0x9C.toByte() || // Level 6
                input[1] == 0xDA.toByte()    // Level 7 - 9
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
