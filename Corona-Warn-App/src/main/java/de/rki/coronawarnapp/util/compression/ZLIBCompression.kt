package de.rki.coronawarnapp.util.compression

import okio.Buffer
import okio.inflate
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
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
        Timber.e(e)
        throw InvalidInputException("ZLIB decompression failed.", e)
    }

    fun compress(input: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(input)
        val outputStream = ByteArrayOutputStream(input.size)
        deflater.finish()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        return outputStream.toByteArray()
    }
}

fun ByteArray.inflate(sizeLimit: Long = -1L) = ZLIBCompression().decompress(this, sizeLimit)

fun ByteArray.deflate() = ZLIBCompression().compress(this)
