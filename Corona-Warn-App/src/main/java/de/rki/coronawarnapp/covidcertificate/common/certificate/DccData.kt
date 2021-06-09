package de.rki.coronawarnapp.covidcertificate.common.certificate

data class DccData<CertT : Dcc<*>>(
    val header: DccHeader,
    val certificate: CertT,
)
