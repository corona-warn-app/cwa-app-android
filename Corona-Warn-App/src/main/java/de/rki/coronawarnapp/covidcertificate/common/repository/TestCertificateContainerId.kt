package de.rki.coronawarnapp.covidcertificate.common.repository

import kotlinx.parcelize.Parcelize

@Parcelize
data class TestCertificateContainerId(override val identifier: String) : CertificateContainerId()
