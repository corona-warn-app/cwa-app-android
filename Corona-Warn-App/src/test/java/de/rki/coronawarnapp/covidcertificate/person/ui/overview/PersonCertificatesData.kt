package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Instant
import java.util.UUID

object PersonCertificatesData {

    val mockTestCertificateWrapper = mockk<TestCertificateWrapper>().apply {
        every { isCertificateRetrievalPending } returns true
        every { isUpdatingData } returns true
        every { registeredAt } returns Instant.EPOCH
        every { containerId } returns TestCertificateContainerId("testCertificateContainerId")
    }
    val certificatesWithPending = mutableSetOf<PersonCertificates>()
        .apply {
            add(PersonCertificates(listOf(testCertificate(fullName = "Andrea Schneider"))))
            add(PersonCertificates(listOf(testCertificate(fullName = "Zeebee")), isCwaUser = true))
        }
    val certificatesWithUpdating = mutableSetOf<PersonCertificates>().apply {
        add(PersonCertificates(listOf(testCertificate(fullName = "Andrea Schneider"))))
        add(PersonCertificates(listOf(testCertificate(fullName = "Zeebee")), isCwaUser = true))
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
    override fun getState(): CwaCovidCertificate.State = CwaCovidCertificate.State.Valid(expiresAt = Instant.EPOCH)

    override val notifiedExpiredAt: Instant?
        get() = null
    override val notifiedExpiresSoonAt: Instant?
        get() = null

    override val containerId: TestCertificateContainerId
        get() = TestCertificateContainerId(UUID.randomUUID().toString())
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
    override val headerIssuer: String = "issuer"
    override val headerIssuedAt: Instant = Instant.EPOCH
    override val headerExpiresAt: Instant = Instant.EPOCH
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

    override val rawCertificate: TestDccV1
        get() = mockk()

    override val dccData: DccData<*>
        get() = mockk()
}
