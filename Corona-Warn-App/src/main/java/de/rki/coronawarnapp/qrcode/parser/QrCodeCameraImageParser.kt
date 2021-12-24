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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class QrCodeCameraImageParser @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {

    private val currentRawResults: MutableSharedFlow<String> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val rawResults: Flow<String> = currentRawResults
        .distinctUntilChanged()
        .filter { it.isNotEmpty() }

    private val mutex = Mutex()
    private val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)

    suspend fun parseQrCode(imageProxy: ImageProxy): Unit = withContext(dispatcherProvider.Default) {
        imageProxy.use { it.toGrayU8().parse() }
    }

    private suspend fun GrayU8.parse() = mutex.withLock {
        Timber.tag(TAG).v("Parsing image")
        val qrCodes = with(detector) {
            process(this@parse)

            if (detections.isEmpty()) {
                // BoofCv has problems with wrongly transposed qr codes. Transposing the image again seems to help
                // https://github.com/lessthanoptimal/BoofCV/issues/313
                // https://github.com/lessthanoptimal/BoofCV/issues/312
                process(transpose())
            }
            detections
        }

        Timber.tag(TAG).v("Found %d qr codes", qrCodes.size)

        when (qrCodes.isEmpty()) {
            true -> listOf("")
            false -> qrCodes.map { it.message }
        }.forEach { currentRawResults.tryEmit(it) }
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

    @SuppressLint("UnsafeOptInUsageError")
    private fun ImageProxy.toGrayU8(): GrayU8 = ImageType.SB_U8.createImage(width, height).also {
        ConvertCameraImage.imageToBoof(image, ColorFormat.RGB, it, null)
    }

    companion object {
        private val TAG = tag<QrCodeCameraImageParser>()
    }
}
