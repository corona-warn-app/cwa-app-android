package de.rki.coronawarnapp.covidcertificate.common.certificate

data class DccData<C : DccV1.MetaData>(
    val header: DccHeader,
    val certificate: C,
)
