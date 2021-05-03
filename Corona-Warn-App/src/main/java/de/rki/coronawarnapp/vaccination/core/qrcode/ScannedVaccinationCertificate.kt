package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.ui.Country
import org.joda.time.LocalDate

interface ScannedVaccinationCertificate {
    val firstName: String
    val firstNameStandardized: String
    val lastName: String
    val lastNameStandardized: String
    val dateOfBirth: LocalDate
    val vaccinatedAt: LocalDate
    val vaccineId: String
    val medicalProductId: String
    val marketAuthorizationHolderId: String
    val chargeId: String
    val certificateIssuer: String
    val certificateCountry: Country
    val certificateId: String
}
