package de.rki.coronawarnapp.covidcertificate.common.certificate

data class DccData(
    val header: DccHeader,
    val certificate: DccV1,
)
