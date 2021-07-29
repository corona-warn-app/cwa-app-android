package de.rki.coronawarnapp.qrcode

import android.net.Uri
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import dagger.Reusable
import de.rki.coronawarnapp.qrcode.provider.QRCodeBitmapProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class QRCodeFileParser @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val qrCodeBitmapProvider: QRCodeBitmapProvider,
    private val qrCodeReader: QRCodeReader
) {

    suspend fun decodeQrCodeFile(fileUri: Uri): QRCodeParseResult = withContext(dispatcherProvider.IO) {
        when (val bitmapResult = qrCodeBitmapProvider.getBitmapsForUri(fileUri)) {
            is QRCodeBitmapProvider.BitmapResult.Success -> {
                for (bitmap in bitmapResult.bitmaps) {
                    val pixelBuffer = IntArray(bitmap.byteCount)
                    bitmap.getPixels(pixelBuffer, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                    bitmap.recycle()

                    val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, pixelBuffer)
                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                    try {
                        val content = qrCodeReader.decode(binaryBitmap).text
                        Timber.d("Parsed qr code from image: %s", content)
                        return@withContext QRCodeParseResult.Success(content)
                    } catch (ex: ReaderException) {
                        Timber.d(ex, "Failed to Parse QR Code from bitmap")
                    }
                }

                return@withContext QRCodeParseResult.Failure(
                    IllegalArgumentException("No valid QR Code found")
                )
            }
            is QRCodeBitmapProvider.BitmapResult.Failed -> {
                QRCodeParseResult.Failure(bitmapResult.error)
            }
        }
    }

    sealed class QRCodeParseResult {
        data class Success(val text: String) : QRCodeParseResult()
        data class Failure(val exception: Exception) : QRCodeParseResult()
    }
}
