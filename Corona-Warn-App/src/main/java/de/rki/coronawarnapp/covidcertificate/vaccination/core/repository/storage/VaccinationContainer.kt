package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccHeader
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

@Keep
data class VaccinationContainer internal constructor(
    @SerializedName("vaccinationQrCode") val vaccinationQrCode: QrCodeString,
    @SerializedName("scannedAt") val scannedAt: Instant,
) {

    // Either set by [ContainerPostProcessor] or via [toVaccinationContainer]
    @Transient lateinit var qrCodeExtractor: DccQrCodeExtractor
    @Transient internal var preParsedData: DccData<VaccinationDccV1>? = null

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this("", Instant.EPOCH)

    @delegate:Transient
    internal val certificateData: DccData<VaccinationDccV1> by lazy {
        preParsedData ?: (
            qrCodeExtractor.extract(
                vaccinationQrCode,
                mode = DccV1Parser.Mode.CERT_VAC_LENIENT
            ) as VaccinationCertificateQRCode
            )
            .data
    }

    val header: DccHeader
        get() = certificateData.header

    val certificate: VaccinationDccV1
        get() = certificateData.certificate

    val vaccination: DccV1.VaccinationData
        get() = certificate.vaccination

    val certificateId: String
        get() = vaccination.uniqueCertificateIdentifier

    val personIdentifier: CertificatePersonIdentifier
        get() = certificate.personIdentifier

    fun toVaccinationCertificate(
        valueSet: VaccinationValueSets?,
        userLocale: Locale = Locale.getDefault(),
    ) = object : VaccinationCertificate {
        override val personIdentifier: CertificatePersonIdentifier
            get() = certificate.personIdentifier

        override val firstName: String?
            get() = certificate.nameData.firstName

        override val lastName: String
            get() = certificate.nameData.lastName

        override val fullName: String
            get() = certificate.nameData.fullName

        override val dateOfBirth: LocalDate
            get() = certificate.dateOfBirth

        override val vaccinatedAt: LocalDate
            get() = vaccination.vaccinatedAt

        override val doseNumber: Int
            get() = vaccination.doseNumber
        override val totalSeriesOfDoses: Int
            get() = vaccination.totalSeriesOfDoses

        override val vaccineTypeName: String
            get() = valueSet?.getDisplayText(vaccination.vaccineId) ?: vaccination.vaccineId
        override val vaccineManufacturer: String
            get() = valueSet?.getDisplayText(vaccination.marketAuthorizationHolderId)
                ?: vaccination.marketAuthorizationHolderId
        override val medicalProductName: String
            get() = valueSet?.getDisplayText(vaccination.medicalProductId) ?: vaccination.medicalProductId

        override val certificateIssuer: String
            get() = vaccination.certificateIssuer
        override val certificateCountry: String
            get() = Locale(
                userLocale.language,
                vaccination.certificateCountry.uppercase()
            ).getDisplayCountry(userLocale)

        override val certificateId: String
            get() = vaccination.uniqueCertificateIdentifier

        override val issuer: String
            get() = header.issuer
        override val issuedAt: Instant
            get() = header.issuedAt
        override val expiresAt: Instant
            get() = header.expiresAt

        override val qrCode: QrCodeString
            get() = vaccinationQrCode
    }
}

fun VaccinationCertificateQRCode.toVaccinationContainer(
    scannedAt: Instant,
    qrCodeExtractor: DccQrCodeExtractor,
) = VaccinationContainer(
    vaccinationQrCode = this.qrCode,
    scannedAt = scannedAt,
).apply {
    this.qrCodeExtractor = qrCodeExtractor
    preParsedData = data
}
