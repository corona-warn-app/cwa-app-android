package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.Parcelize

@Parcelize
class TestCertificateContainerId(override val qrCodeHash: String) : CertificateContainerId()
