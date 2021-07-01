package de.rki.coronawarnapp.covidcertificate.common.certificate

data class DccData<CertT : DccV1.MetaData>(
    val header: DccHeader,
    val certificate: CertT,
    val certificateJson: String
)
