package de.rki.coronawarnapp.vaccination.core.qrcode

import okio.ByteString

// TODO
data class VaccinationCertificateQRCode(
    // Vaccine or prophylaxis
    val certificate: ScannedVaccinationCertificate,
    val qrCodeOriginalBase45: String,
    val qrCodeOriginalCBOR: ByteString,
)
