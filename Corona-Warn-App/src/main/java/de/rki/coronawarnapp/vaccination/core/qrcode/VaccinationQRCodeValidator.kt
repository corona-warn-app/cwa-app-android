package de.rki.coronawarnapp.vaccination.core.qrcode

import dagger.Reusable
import javax.inject.Inject

@Reusable
class VaccinationQRCodeValidator @Inject constructor() {

    fun validate(raw: String): VaccinationCertificateQRCode {
        throw NotImplementedError()
    }
}
