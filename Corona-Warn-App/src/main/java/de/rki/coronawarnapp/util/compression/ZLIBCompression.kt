package de.rki.coronawarnapp.util.compression

import okio.Buffer
import okio.ByteString
import okio.inflate
import java.util.zip.Inflater
import javax.inject.Inject

class ZLIBCompression @Inject constructor() {
    fun decompress(input: ByteString, sizeLimit: Long = -1L): ByteString = try {
        val inflaterSource = input.let {
            val buffer = Buffer().write(it)
            buffer.inflate(Inflater())
        }

        val sink = Buffer()

        sink.use { sinkBuffer ->
            inflaterSource.use {
                val aboveLimit = if (sizeLimit > 0) sizeLimit + 1L else Long.MAX_VALUE
                val inflated = it.readOrInflate(sinkBuffer, aboveLimit)
                if (inflated == aboveLimit) {
                    throw InvalidInputException("Inflated size exceeds $sizeLimit")
                }
            }
        }

        sink.readByteString()
    } catch (e: Throwable) {
        throw InvalidInputException("ZLIB decompression failed.", e)
    }
}

fun ByteString.inflate(sizeLimit: Long = -1L) = ZLIBCompression().decompress(this, sizeLimit)
