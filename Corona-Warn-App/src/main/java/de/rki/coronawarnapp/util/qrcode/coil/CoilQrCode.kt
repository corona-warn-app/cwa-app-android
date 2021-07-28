package de.rki.coronawarnapp.util.qrcode.coil

import de.rki.coronawarnapp.util.HashExtensions.toSHA1
import de.rki.coronawarnapp.util.qrcode.QrCodeOptions

data class CoilQrCode(
    val content: String,
    val options: QrCodeOptions = QrCodeOptions(),
) {
    // Needs to be uniquely identify this
    val requestKey by lazy {
        "${content.toSHA1()}#${options.optionsKey}"
    }
}
