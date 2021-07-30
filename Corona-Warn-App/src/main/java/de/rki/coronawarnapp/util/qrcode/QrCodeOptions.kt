package de.rki.coronawarnapp.util.qrcode

import android.os.Parcel
import android.os.Parcelable
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.util.HashExtensions.toSHA1
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.nio.charset.Charset

/**
 * @param correctionLevel [ErrorCorrectionLevel]
 * @param characterSet [String]
 */
@Parcelize
@TypeParceler<Charset, CharsetParceler>()
data class QrCodeOptions(
    val correctionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    val characterSet: Charset = Charsets.UTF_8,
) : Parcelable {
    @IgnoredOnParcel val optionsKey by lazy {
        "#$correctionLevel#$characterSet".toSHA1()
    }
}

private object CharsetParceler : Parceler<Charset> {
    override fun create(parcel: Parcel): Charset = Charset.forName(parcel.readString())

    override fun Charset.write(parcel: Parcel, flags: Int) {
        parcel.writeString(name())
    }
}
