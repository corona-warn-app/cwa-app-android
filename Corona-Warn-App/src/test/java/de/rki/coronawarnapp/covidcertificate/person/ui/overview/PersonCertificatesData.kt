package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import org.joda.time.Instant

object PersonCertificatesData {
    val certificatesWithPending = mutableSetOf<PersonCertificates>()
        .apply {
            add(PersonCertificates(listOf(testCertificate(fullName = "Andrea Schneider"))))
            add(PersonCertificates(listOf(testCertificate(fullName = "Max Mustermann", isPending = true))))
            add(PersonCertificates(listOf(testCertificate(fullName = "Zeebee")), isCwaUser = true))
        }
    val certificatesWithUpdating = mutableSetOf<PersonCertificates>().apply {
        add(PersonCertificates(listOf(testCertificate(fullName = "Andrea Schneider"))))
        add(PersonCertificates(listOf(testCertificate(fullName = "Zeebee")), isCwaUser = true))
        add(
            PersonCertificates(
                listOf(testCertificate(fullName = "Max Mustermann", isPending = true, isUpdating = true))
            )
        )
    }
    val certificatesWithCwaUser = mutableSetOf<PersonCertificates>().apply {
        add(PersonCertificates(listOf(testCertificate(fullName = "Max Mustermann"))))
        add(PersonCertificates(listOf(testCertificate(fullName = "Erika Musterfrau"))))
        add(PersonCertificates(listOf(testCertificate(fullName = "Andrea Schneider"))))
        add(PersonCertificates(listOf(testCertificate(fullName = "Zeebee")), isCwaUser = true))
        add(PersonCertificates(listOf(testCertificate(fullName = "Zeebee A"))))
    }
    val certificatesWithoutCwaUser = mutableSetOf<PersonCertificates>().apply {
        add(PersonCertificates(listOf(testCertificate("Max Mustermann"))))
        add(PersonCertificates(listOf(testCertificate("Erika Musterfrau"))))
        add(PersonCertificates(listOf(testCertificate("Andrea Schneider"))))
    }
}

fun testCertificate(
    fullName: String,
    isPending: Boolean = false,
    isUpdating: Boolean = false
) = object : TestCertificate {
    override val targetName: String = "targetName"
    override val testType: String = "testType"
    override val testResult: String = "testResult"
    override val testName: String = "testName"
    override val testNameAndManufacturer: String = "testNameAndManufacturer"
    override val sampleCollectedAt: Instant = Instant.EPOCH
    override val sampleCollectedAtFormatted: String = ""
    override val testCenter: String = ""
    override val registeredAt: Instant = Instant.EPOCH
    override val isUpdatingData: Boolean = isUpdating
    override val isCertificateRetrievalPending: Boolean = isPending
    override val issuer: String = "issuer"
    override val issuedAt: Instant = Instant.EPOCH
    override val expiresAt: Instant = Instant.EPOCH
    override val qrCode: QrCodeString = "qrCode"
    override val firstName: String = "firstName"
    override val lastName: String = "lastName"
    override val fullName: String = fullName
    override val dateOfBirthFormatted = "1981-03-20"
    override val personIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1981-03-20",
        lastNameStandardized = "lastNameStandardized",
        firstNameStandardized = "firstNameStandardized"
    )
    override val certificateIssuer: String = "certificateIssuer"
    override val certificateCountry: String = "certificateCountry"
    override val certificateId: String = "certificateId"
}
