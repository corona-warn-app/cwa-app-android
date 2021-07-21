package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

object QrCodeHelper {

    fun isInvalidOrExpiredQrCode(state: CwaCovidCertificate.State): Boolean {
        return when (state) {
            is CwaCovidCertificate.State.Invalid,
            is CwaCovidCertificate.State.Expired -> {
                true
            }
            is CwaCovidCertificate.State.ExpiringSoon,
            is CwaCovidCertificate.State.Valid -> {
                false
            }
        }
    }
}

