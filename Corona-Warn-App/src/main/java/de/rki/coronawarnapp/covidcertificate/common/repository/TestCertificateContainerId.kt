package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class TestCertificateContainerId(private val certUuid: String) : CertificateContainerId() {
    @IgnoredOnParcel
    override val identifier: String
        get() = certUuid
}
