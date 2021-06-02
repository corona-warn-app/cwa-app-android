package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.qrcode.QrCodeString
import org.joda.time.Instant
import org.joda.time.LocalDate

interface VaccinationCertificate {
    val firstName: String?
    val lastName: String

    val dateOfBirth: LocalDate
    val vaccinatedAt: LocalDate

    val vaccineTypeName: String
    val vaccineManufacturer: String
    val medicalProductName: String

    val doseNumber: Int
    val totalSeriesOfDoses: Int

    val certificateIssuer: String
    val certificateCountry: String
    val certificateId: String

    val personIdentifier: VaccinatedPersonIdentifier

    val issuer: String
    val issuedAt: Instant
    val expiresAt: Instant

    val vaccinationQrCodeString: QrCodeString
}
