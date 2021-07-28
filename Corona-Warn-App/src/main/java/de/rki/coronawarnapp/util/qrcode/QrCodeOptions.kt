package de.rki.coronawarnapp.util.qrcode

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.util.HashExtensions.toSHA1
import java.nio.charset.Charset

/**
 * @param correctionLevel [ErrorCorrectionLevel]
 * @param characterSet [String]
 */
data class QrCodeOptions(
    val correctionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    val characterSet: Charset = Charsets.UTF_8,
) {
    val optionsKey by lazy {
        "#$correctionLevel#$characterSet".toSHA1()
    }
}
