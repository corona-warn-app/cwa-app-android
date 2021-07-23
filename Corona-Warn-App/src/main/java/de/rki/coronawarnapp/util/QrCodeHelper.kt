package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

object QrCodeHelper {

    fun isInvalidOrExpired(state: CwaCovidCertificate.State): Boolean {
        return true
        /* return when (state) {
            is CwaCovidCertificate.State.Invalid,
            is CwaCovidCertificate.State.Expired -> {
                true
            }
            is CwaCovidCertificate.State.ExpiringSoon,
            is CwaCovidCertificate.State.Valid -> {
                false
            }
        } */
    }

    const val sampleQrCodeText =
        "https://www.bundesregierung.de/breg-de/themen/corona-warn-app/corona-warn-app-faq-1758392"
}
