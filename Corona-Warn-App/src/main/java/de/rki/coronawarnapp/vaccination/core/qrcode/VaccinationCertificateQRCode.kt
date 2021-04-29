package de.rki.coronawarnapp.vaccination.core.qrcode

// TODO
data class VaccinationCertificateQRCode(
    // Vaccine or prophylaxis
    val vaccineNameId: String,
    val vaccineMedicinalProduct: String,
    val marketAuthorizationHolder: String,
)
