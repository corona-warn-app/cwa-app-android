package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccMaxPersonCheckerTest : BaseTest() {

    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var configProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var qrCode: VaccinationCertificateQRCode

    val existingIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1980-10-10",
        firstNameStandardized = "firstNameStandardized",
        lastNameStandardized = "lastNameStandardized"
    )

    val existingIdentifier2 = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1980-10-10",
        firstNameStandardized = "firstNameStandardized2",
        lastNameStandardized = "lastNameStandardized2"
    )

    val newIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1990-10-10",
        firstNameStandardized = "firstNameStandardized1",
        lastNameStandardized = "lastNameStandardized1"
    )

    val vaccinationCertificate = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns existingIdentifier
    }
    val personCertificate = PersonCertificates(certificates = listOf(vaccinationCertificate))

    val vaccinationCertificate2 = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns existingIdentifier2
    }
    val personCertificate2 = PersonCertificates(certificates = listOf(vaccinationCertificate2))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { personCertificatesProvider.personCertificates } returns flowOf(
            setOf(
                personCertificate,
                personCertificate2
            )
        )
    }

    @Test
    fun `happy path below threshold results in PASSED`() = runBlockingTest {
        every { configData.dccPersonWarnThreshold } returns 3
        every { configData.dccPersonCountMax } returns 4
        coEvery { configProvider.currentConfig } returns flowOf(configData)
        every { qrCode.personIdentifier } returns newIdentifier
        createInstance().checkForMaxPersons(
            qrCode
        ) shouldBe DccMaxPersonChecker.Result.PASSED
    }

    @Test
    fun `new person exceeds threshold results in EXCEEDS_THRESHOLD`() = runBlockingTest {
        every { configData.dccPersonWarnThreshold } returns 1
        every { configData.dccPersonCountMax } returns 3
        coEvery { configProvider.currentConfig } returns flowOf(configData)
        every { qrCode.personIdentifier } returns newIdentifier
        createInstance().checkForMaxPersons(
            qrCode
        ) shouldBe DccMaxPersonChecker.Result.EXCEEDS_THRESHOLD
    }

    @Test
    fun `exceeds threshold but not a new person results in PASSED`() = runBlockingTest {
        every { configData.dccPersonWarnThreshold } returns 1
        every { configData.dccPersonCountMax } returns 3
        coEvery { configProvider.currentConfig } returns flowOf(configData)
        every { qrCode.personIdentifier } returns existingIdentifier
        createInstance().checkForMaxPersons(
            qrCode
        ) shouldBe DccMaxPersonChecker.Result.PASSED

    }

    @Test
    fun `new person exceeds max results in EXCEEDS_MAX`() = runBlockingTest {
        every { configData.dccPersonWarnThreshold } returns 1
        every { configData.dccPersonCountMax } returns 2
        coEvery { configProvider.currentConfig } returns flowOf(configData)
        every { qrCode.personIdentifier } returns newIdentifier
        createInstance().checkForMaxPersons(
            qrCode
        ) shouldBe DccMaxPersonChecker.Result.EXCEEDS_MAX
    }

    @Test
    fun `exceeds max but not a new person results in PASSED`() = runBlockingTest {
        every { configData.dccPersonWarnThreshold } returns 1
        every { configData.dccPersonCountMax } returns 1
        coEvery { configProvider.currentConfig } returns flowOf(configData)
        every { qrCode.personIdentifier } returns existingIdentifier
        createInstance().checkForMaxPersons(
            qrCode
        ) shouldBe DccMaxPersonChecker.Result.PASSED
    }

    private fun createInstance() = DccMaxPersonChecker(
        personCertificatesProvider,
        configProvider
    )
}
