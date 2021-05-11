package de.rki.coronawarnapp.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.personIdentifier
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateCOSEParser
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateV1
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.getDisplayText

import okio.ByteString
import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class VaccinationContainer(
    @SerializedName("vaccinationCertificateCOSE") val vaccinationCertificateCOSE: ByteString,
    @SerializedName("scannedAt") val scannedAt: Instant,
) {

    @Transient internal var preParsedData: VaccinationCertificateData? = null

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(ByteString.EMPTY, Instant.EPOCH)

    @delegate:Transient
    private val certificateData: VaccinationCertificateData by lazy {
        preParsedData ?: VaccinationCertificateCOSEParser().parse(vaccinationCertificateCOSE)
    }

    val certificate: VaccinationCertificateV1
        get() = certificateData.vaccinationCertificate

    val vaccination: VaccinationCertificateV1.VaccinationData
        get() = certificate.vaccinationDatas.single()

    val certificateId: String
        get() = vaccination.uniqueCertificateIdentifier

    val personIdentifier: VaccinatedPersonIdentifier
        get() = certificate.personIdentifier

    val isEligbleForProofCertificate: Boolean
        get() = vaccination.doseNumber == vaccination.totalSeriesOfDoses

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
    }
}

fun VaccinationCertificateQRCode.toVaccinationContainer(scannedAt: Instant) = VaccinationContainer(
    vaccinationCertificateCOSE = certificateCOSE,
    scannedAt = scannedAt,
).apply {
    preParsedData = parsedData
}
