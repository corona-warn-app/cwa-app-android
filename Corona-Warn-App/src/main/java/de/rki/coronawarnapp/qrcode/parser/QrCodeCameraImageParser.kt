package de.rki.coronawarnapp.qrcode.parser

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import boofcv.alg.color.ColorFormat
import boofcv.android.ConvertCameraImage
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageType
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class QrCodeCameraImageParser @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {

    private val mutex = Mutex()
    private val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)

    suspend fun parseQrCode(
        imageProxy: ImageProxy,
        onResult: suspend (Set<String>) -> Unit
    ): Unit = withContext(dispatcherProvider.Default)
    {
        imageProxy.use { image ->
            val rawResults = image.toGrayU8().parse()
            // Execute within 'use' because image analyzer gets blocked until the image proxy gets closed
            onResult(rawResults)
        }
    }

    private suspend fun GrayU8.parse(): Set<String> = mutex.withLock {
        Timber.tag(TAG).v("Parsing image")
        with(detector) {
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

    private fun GrayU8.transpose(): GrayU8 {
        val transposed = ImageType.SB_U8.createImage(height, width)
        for (x in 0 until width) {
            for (y in 0 until height) {
                transposed[y, x] = this[x, y]
            }
        }
        return transposed
    }

    companion object {
        private val TAG = tag<QrCodeCameraImageParser>()
    }
}
