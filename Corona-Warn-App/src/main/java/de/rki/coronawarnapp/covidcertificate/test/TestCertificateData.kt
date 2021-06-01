package de.rki.coronawarnapp.covidcertificate.test

import de.rki.coronawarnapp.vaccination.core.certificate.CoseCertificateHeader

data class TestCertificateData(
    val header: CoseCertificateHeader,
    val certificate: TestCertificateDccV1,
)
