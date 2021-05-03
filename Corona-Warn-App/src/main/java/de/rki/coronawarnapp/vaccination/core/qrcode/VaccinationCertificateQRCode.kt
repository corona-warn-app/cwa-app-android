package de.rki.coronawarnapp.vaccination.core.qrcode

import okio.ByteString

// TODO
data class VaccinationCertificateQRCode(
    val certificate: ScannedVaccinationCertificate,
    val qrCodeOriginalBase45: String,
    val qrCodeOriginalCBOR: ByteString,
)
