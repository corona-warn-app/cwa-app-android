package de.rki.coronawarnapp.util.qrcode.coil

import android.os.Parcelable
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.util.HashExtensions.toSHA1
import de.rki.coronawarnapp.util.qrcode.QrCodeOptions
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoilQrCode(
    val content: QrCodeString,
    val options: QrCodeOptions = QrCodeOptions(),
) : Parcelable {
    // Needs to uniquely identify this
    @IgnoredOnParcel val requestKey by lazy {
        "${content.toSHA1()}#${options.optionsKey}"
    }
}
