package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class RecoveryCertificateContainerId(private val qrcodeHash: String) : CertificateContainerId() {
    @IgnoredOnParcel
    override val identifier: String
        get() = qrcodeHash
}
