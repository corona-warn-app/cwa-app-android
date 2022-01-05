package de.rki.coronawarnapp.qrcode.parser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import boofcv.alg.color.ColorFormat
import boofcv.android.ConvertBitmap
import boofcv.android.ConvertCameraImage
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageType
import de.rki.coronawarnapp.tag
import timber.log.Timber
import kotlin.Exception

class QrCodeBoofCVParser {

    private val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)

    fun parseQrCode(bitmap: Bitmap): ParseResult = try {
        bitmap
            .toGrayU8()
            .parse()
            .toParseResultSuccess()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse bitmap=%s", bitmap)
        e.toParseResultFailure()
    }

    fun parseQrCode(imageProxy: ImageProxy): ParseResult = try {
        imageProxy
            .toGrayU8()
            .parse()
            .toParseResultSuccess()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse image proxy=%s", imageProxy)
        e.toParseResultFailure()
    }

    private fun GrayU8.parse(): Set<String> {
        Timber.tag(TAG).v("Parsing image")
        return with(detector) {
            process(this@parse)

            if (detections.isEmpty()) {
                // BoofCv has problems with wrongly transposed qr codes. Transposing the image again seems to help
                // https://github.com/lessthanoptimal/BoofCV/issues/313
                // https://github.com/lessthanoptimal/BoofCV/issues/312
                process(transpose())
            }
            detections
        }
            .map { it.message }
            .toSet()
            .also { Timber.tag(TAG).v("Found %d qr codes", it.size) }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun ImageProxy.toGrayU8(): GrayU8 = ImageType.SB_U8.createImage(width, height).also {
        ConvertCameraImage.imageToBoof(image, ColorFormat.RGB, it, null)
    }

    private fun Bitmap.toGrayU8(): GrayU8 = ImageType.SB_U8.createImage(width, height).also {
        ConvertBitmap.bitmapToBoof(this, it, null)
    }

    private fun GrayU8.transpose(): GrayU8 {
        val transposed = ImageType.SB_U8.createImage(height, width)
        for (x in 0 until width) {
            for (y in 0 until height) {
                transposed[y, x] = this[x, y]
            }
        }
        return transposed
    }

    private fun Set<String>.toParseResultSuccess() = ParseResult.Success(rawResults = this)
    private fun Exception.toParseResultFailure() = ParseResult.Failure(exception = this)

    sealed class ParseResult {
        data class Success(val rawResults: Set<String>) : ParseResult() {
            val isNotEmpty: Boolean get() = rawResults.isNotEmpty()
        }

        data class Failure(val exception: Exception) : ParseResult()
    }

    companion object {
        private val TAG = tag<QrCodeBoofCVParser>()
    }
}
