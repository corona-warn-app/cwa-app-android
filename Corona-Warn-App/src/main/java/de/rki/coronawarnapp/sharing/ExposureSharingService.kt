package de.rki.coronawarnapp.sharing

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.google.android.gms.common.api.ApiException
import com.google.protobuf.ByteString
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import de.rki.coronawarnapp.exception.ExceptionCategory.EXPOSURENOTIFICATION
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import timber.log.Timber

object ExposureSharingService {
    private const val defaultWidth: Int = 150
    private const val defaultHeight: Int = 150

    suspend fun shareKeysAsBitmap(
        width: Int?,
        height: Int?,
        callback: (Bitmap?) -> Unit?
    ) {
        try {
            val tempExpKeys = InternalExposureNotificationClient
                .asyncGetTemporaryExposureKeyHistory()

            val latest = tempExpKeys.maxBy { it.rollingStartIntervalNumber }

            val key = AppleLegacyKeyExchange.Key.newBuilder()
                .setKeyData(ByteString.copyFrom(latest!!.keyData))
                .setRollingPeriod(latest.rollingPeriod)
                .setRollingStartNumber(latest.rollingStartIntervalNumber)
                .build().toByteArray()
            val bMatrix = QRCodeWriter().encode(
                Base64.encodeToString(key, Base64.DEFAULT),
                BarcodeFormat.QR_CODE,
                width ?: defaultWidth,
                height ?: defaultHeight
            )
            val bmp =
                Bitmap.createBitmap(bMatrix.width, bMatrix.height, Bitmap.Config.RGB_565)
            for (x in 0 until bMatrix.width) {
                for (y in 0 until bMatrix.height) {
                    bmp.setPixel(x, y, if (bMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            Timber.d("Bitmap generated")
            callback(bmp)
        } catch (e: ApiException) {
            e.report(EXPOSURENOTIFICATION)
            callback(null)
        } catch (e: Exception) {
            e.report(INTERNAL)
            callback(null)
        }
    }

    fun getOthersKeys(
        rawData: String?,
        callback: (AppleLegacyKeyExchange.Key?) -> Unit?
    ) {
        try {
            val decodedQr = Base64.decode(rawData, Base64.DEFAULT)
            val key = AppleLegacyKeyExchange.Key.parseFrom(decodedQr)
            callback(key)
        } catch (ex: Exception) {
            Timber.d("$ex")
            callback(null)
        }
    }
}
