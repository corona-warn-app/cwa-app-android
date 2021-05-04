package de.rki.coronawarnapp.vaccination.core.qrcode

import java.util.zip.InflaterInputStream
import javax.inject.Inject

/**
 * Compresses/decompresses input with ZLIB, [level] specifies the compression level (0-9)
 */
class ZlibDecompressor @Inject constructor() {
    /**
     * *Optionally* decompresses input with ZLIB = inflating.
     *
     * If the [input] does not start with ZLIB magic numbers (0x78), no decompression happens
     */
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
                    input
                }
            }
        }
        return input
    }
}
