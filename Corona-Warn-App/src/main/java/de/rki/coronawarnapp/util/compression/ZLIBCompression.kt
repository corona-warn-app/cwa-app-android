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

//    fun compress(input: ByteArray): ByteArray {
//        // Compress the bytes
//        val output = ByteArray(input.size)
//        val compressor = Deflater()
//        compressor.setInput(input)
//        compressor.finish()
//        val compressedDataLength = compressor.deflate(output)
//        compressor.end()
//        return output
//    }

    fun compress(data: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(data)
        val outputStream = ByteArrayOutputStream(data.size)
        deflater.finish()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer) // returns the generated code... index
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val output: ByteArray = outputStream.toByteArray()
        return output
    }
}

fun ByteArray.inflate(sizeLimit: Long = -1L) = ZLIBCompression().decompress(this, sizeLimit)

fun ByteArray.deflate() = ZLIBCompression().compress(this)
