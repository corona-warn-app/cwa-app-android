package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class VaccinationCertificateContainerId(private val certificateId: String) : CertificateContainerId() {
    @IgnoredOnParcel
    override val identifier: String
        get() = certificateId
}
