package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.main.CWASettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class MigrationCheckTest {

    @MockK private lateinit var cwaSettings: CWASettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { cwaSettings.wasCertificateGroupingMigrationAcknowledged } returns false
        every { cwaSettings.wasCertificateGroupingMigrationAcknowledged = any() } just Runs
    }

    @Test
    fun `don't show migration info when no certificate is available`() {
        val persons = emptySet<PersonCertificates>()

        getInstance().shouldShowMigrationInfo(persons) shouldBe false
    }

    @Test
    fun `don't show migration info when no change happens`() {
        val certificate1 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Surname1",
                firstNameStandardized = "Name1")
        }
        val certificate2 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Surname2",
                firstNameStandardized = "Name2")
        }
        val person1 = mockk<PersonCertificates>().apply {
            every { certificates } returns listOf(certificate1)
        }
        val person2 = mockk<PersonCertificates>().apply {
            every { certificates } returns listOf(certificate2)
        }
        val persons = setOf(person1, person2)

        getInstance().shouldShowMigrationInfo(persons) shouldBe false
    }

    @Test
    fun `show migration info when change happens`() {
        val certificate1 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Surname",
                firstNameStandardized = "Name")
        }
        val certificate2 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Name",
                firstNameStandardized = "Surname")
        }
        val person1 = mockk<PersonCertificates>().apply {
            every { certificates } returns listOf(certificate1, certificate2)
        }

        val persons = setOf(person1)

        getInstance().shouldShowMigrationInfo(persons) shouldBe true
    }

    private fun getInstance() = MigrationCheck(cwaSettings)
}
