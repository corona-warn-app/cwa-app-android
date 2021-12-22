package de.rki.coronawarnapp.covidcertificate.common.repository

import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import java.util.Locale

interface CertificateRepoContainer : Recyclable {
    val containerId: CertificateContainerId

    val qrCodeHash: String

    /**
     * Returns what qr code to display based on certificate state
     * @param state Certificate state
     * @param language current use locale language such as [Locale.GERMAN.language]
     * @param certificateQrCode [String] Qr Code string
     *
     */
    fun displayQrCode(certificateQrCode: String) = CoilQrCode(certificateQrCode)
}
