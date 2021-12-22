package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.Parcelize

@Parcelize
class RecoveryCertificateContainerId(override val qrCodeHash: String) : CertificateContainerId()
