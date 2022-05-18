package de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts

import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz

internal fun List<DccQrCode>.sort() = sortedByDescending {
    when (it) {
        is TestCertificateQRCode -> it.data.certificate.test.sampleCollectedAt?.toLocalDateUserTz()
        is VaccinationCertificateQRCode -> it.data.certificate.vaccination.vaccinatedOn
        is RecoveryCertificateQRCode -> it.data.certificate.recovery.testedPositiveOn
        else -> null
    }
}
