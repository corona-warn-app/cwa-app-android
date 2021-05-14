package de.rki.coronawarnapp.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.certificate.CoseCertificateHeader
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1
import de.rki.coronawarnapp.vaccination.core.personIdentifier
import de.rki.coronawarnapp.vaccination.core.qrcode.QrCodeString
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQRCodeExtractor
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.getDisplayText
import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class VaccinationContainer internal constructor(
    @SerializedName("vaccinationQrCode") val vaccinationQrCode: QrCodeString,
    @SerializedName("scannedAt") val scannedAt: Instant,
) {

    // Either set by [ContainerPostProcessor] or via [toVaccinationContainer]
    @Transient lateinit var qrCodeExtractor: VaccinationQRCodeExtractor
    @Transient internal var preParsedData: VaccinationCertificateData? = null

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this("", Instant.EPOCH)

    @delegate:Transient
    private val certificateData: VaccinationCertificateData by lazy {
        preParsedData ?: qrCodeExtractor.extract(vaccinationQrCode).parsedData
    }

    val header: CoseCertificateHeader
        get() = certificateData.header

    val certificate: VaccinationDGCV1
        get() = certificateData.certificate

    val vaccination: VaccinationDGCV1.VaccinationData
        get() = certificate.vaccinationDatas.single()

    val certificateId: String
        get() = vaccination.uniqueCertificateIdentifier

    val personIdentifier: VaccinatedPersonIdentifier
        get() = certificate.personIdentifier

    fun toVaccinationCertificate(valueSet: VaccinationValueSet?) = object : VaccinationCertificate {
        override val personIdentifier: VaccinatedPersonIdentifier
            get() = certificate.personIdentifier

        override val firstName: String?
            get() = certificate.nameData.givenName
        override val lastName: String
            get() = certificate.nameData.familyName ?: certificate.nameData.familyNameStandardized

        override val dateOfBirth: LocalDate
            get() = certificate.dateOfBirth

        override val vaccinatedAt: LocalDate
            get() = vaccination.vaccinatedAt

        override val doseNumber: Int
            get() = vaccination.doseNumber
        override val totalSeriesOfDoses: Int
            get() = vaccination.totalSeriesOfDoses

        override val vaccineName: String
            get() = valueSet?.getDisplayText(vaccination.vaccineId) ?: vaccination.vaccineId
        override val vaccineManufacturer: String
            get() = valueSet?.getDisplayText(vaccination.marketAuthorizationHolderId)
                ?: vaccination.marketAuthorizationHolderId
        override val medicalProductName: String
            get() = valueSet?.getDisplayText(vaccination.medicalProductId) ?: vaccination.medicalProductId

        override val certificateIssuer: String
            get() = vaccination.certificateIssuer
        override val certificateCountry: Country
            get() = Country.values().singleOrNull { it.code == vaccination.countryOfVaccination } ?: Country.DE
        override val certificateId: String
            get() = vaccination.uniqueCertificateIdentifier

        override val issuer: String
            get() = header.issuer
        override val issuedAt: Instant
            get() = header.issuedAt
        override val expiresAt: Instant
            get() = header.expiresAt

        override val vaccinationQrCodeString: QrCodeString
            get() = vaccinationQrCode
    }
}

fun VaccinationCertificateQRCode.toVaccinationContainer(
    scannedAt: Instant,
    qrCodeExtractor: VaccinationQRCodeExtractor,
) = VaccinationContainer(
    vaccinationQrCode = this.qrCodeString,
    scannedAt = scannedAt,
).apply {
    this.qrCodeExtractor = qrCodeExtractor
    preParsedData = parsedData
}
