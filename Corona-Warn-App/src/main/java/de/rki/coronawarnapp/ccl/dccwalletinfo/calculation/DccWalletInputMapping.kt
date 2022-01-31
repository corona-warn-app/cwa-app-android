package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

internal fun CwaCovidCertificate.State.toCclState(): CclCertificate.Validity = when (this) {
    CwaCovidCertificate.State.Blocked -> CclCertificate.Validity.BLOCKED
    is CwaCovidCertificate.State.Expired -> CclCertificate.Validity.EXPIRED
    is CwaCovidCertificate.State.ExpiringSoon -> CclCertificate.Validity.EXPIRING_SOON
    is CwaCovidCertificate.State.Invalid -> CclCertificate.Validity.INVALID
    is CwaCovidCertificate.State.Valid -> CclCertificate.Validity.VALID
    else -> throw IllegalStateException("State not supported")
}
