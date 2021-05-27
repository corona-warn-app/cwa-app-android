package de.rki.coronawarnapp.util.compression

import okio.Buffer
import okio.inflate
import java.util.zip.Inflater
import javax.inject.Inject

class ZLIBCompression @Inject constructor() {
    @Suppress("NestedBlockDepth")
    fun decompress(input: ByteArray, sizeLimit: Long = -1L): ByteArray = try {
        val inflaterSource = input.let {
            val buffer = Buffer().write(it)
            buffer.inflate(Inflater())
        }

        val sink = Buffer()

        sink.use { sinkBuffer ->
            inflaterSource.use {
                val aboveLimit = if (sizeLimit > 0) sizeLimit + 1L else Long.MAX_VALUE
                val inflated = it.readOrInflate(sinkBuffer, aboveLimit)
                if (inflated == aboveLimit) throw InvalidInputException("Inflated size exceeds $sizeLimit")
            }
        }

        sink.readByteArray()
    } catch (e: Throwable) {
        throw InvalidInputException("ZLIB decompression failed.", e)
    }
}

fun ByteArray.inflate(sizeLimit: Long = -1L) = ZLIBCompression().decompress(this, sizeLimit)
