package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.main.CWASettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class MigrationCheckTest : BaseTest() {

    @MockK private lateinit var cwaSettings: CWASettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { cwaSettings.wasCertificateGroupingMigrationAcknowledged } returns flowOf(false)
        coEvery { cwaSettings.updateWasCertificateGroupingMigrationAcknowledged(any()) } just Runs
    }

    @Test
    fun `don't show migration info when no certificate is available`() = runTest {
        val persons = emptySet<PersonCertificates>()

        getInstance().shouldShowMigrationInfo(persons) shouldBe false
        coVerify { cwaSettings.updateWasCertificateGroupingMigrationAcknowledged(true) }
    }

    @Test
    fun `don't show migration info when no change happens`() = runTest {
        val certificate1 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Surname1",
                firstNameStandardized = "Name1"
            )
        }
        val certificate2 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Surname2",
                firstNameStandardized = "Name2"
            )
        }
        val person1 = mockk<PersonCertificates>().apply {
            every { certificates } returns listOf(certificate1)
        }
        val person2 = mockk<PersonCertificates>().apply {
            every { certificates } returns listOf(certificate2)
        }
        val persons = setOf(person1, person2)

        getInstance().shouldShowMigrationInfo(persons) shouldBe false
        coVerify { cwaSettings.updateWasCertificateGroupingMigrationAcknowledged(true) }
    }

    @Test
    fun `show migration info when change happens`() = runTest {
        val certificate1 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Surname",
                firstNameStandardized = "Name"
            )
        }
        val certificate2 = mockk<CwaCovidCertificate>().apply {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = "2020-01-01",
                lastNameStandardized = "Name",
                firstNameStandardized = "Surname"
            )
        }
        val person1 = mockk<PersonCertificates>().apply {
            every { certificates } returns listOf(certificate1, certificate2)
        }

        val persons = setOf(person1)

        getInstance().shouldShowMigrationInfo(persons) shouldBe true
        coVerify { cwaSettings.updateWasCertificateGroupingMigrationAcknowledged(true) }
    }

    private fun getInstance() = MigrationCheck(cwaSettings)
}
