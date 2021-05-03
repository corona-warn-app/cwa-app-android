package de.rki.coronawarnapp.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateV1
import okio.ByteString
import org.joda.time.Instant
import org.joda.time.LocalDate

data class VaccinationContainer(
    @SerializedName("certificateV1") val certificateV1: VaccinationCertificateV1,
    @SerializedName("qrCodeOriginalBase45") val qrCodeOriginalBase45: String,
    @SerializedName("qrCodeOriginalCBOR") val qrCodeOriginalCBOR: ByteString,
    @SerializedName("scannedAt") override val scannedAt: Instant,
) : VaccinationCertificate {

    override val personIdentifier: VaccinatedPersonIdentifier
        get() {
            val dob = certificateV1.dateOfBirth.toString()
            val lastName = certificateV1.nameData.familyNameStandardized
            val firstName = certificateV1.nameData.givenNameStandardized
            return "$dob#$lastName#$firstName"
        }

    override val firstName: String
        get() = certificateV1.nameData.givenName
    override val lastName: String
        get() = certificateV1.nameData.familyName
    override val dateOfBirth: LocalDate
        get() = certificateV1.dateOfBirth
    override val vaccinatedAt: Instant
        get() = certificateV1.vaccinationData.vaccinatedAt.toDateTimeAtStartOfDay().toInstant()
    override val vaccineName: String
        get() = TODO("Not yet implemented")
    override val vaccineManufacturer: String
        get() = TODO("Not yet implemented")
    override val chargeId: String
        get() = "??????????"
    override val certificateIssuer: String
        get() = certificateV1.vaccinationData.certificateIssuer
    override val certificateCountry: Country
        get() = Country.values().singleOrNull {
            it.code == certificateV1.vaccinationData.countryOfVaccination
        } ?: Country.DE
    override val certificateId: String
        get() = certificateV1.vaccinationData.uniqueCertificateIdentifier
}
