package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class BoosterNotificationServiceTest : BaseTest() {

    @MockK lateinit var boosterNotification: BoosterNotification
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var dccBoosterRulesValidator: DccBoosterRulesValidator
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { covidCertificateSettings.lastDccBoosterCheck } returns mockFlowPreference(Instant.EPOCH)
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-01T00:00:00.000Z")
        every { personCertificatesProvider.personCertificates } returns flowOf(emptySet())
        every { boosterNotification.showBoosterNotification(any()) } just Runs

        coEvery { vaccinationRepository.updateBoosterNotifiedAt(any(), any()) } just Runs
        coEvery { dccBoosterRulesValidator.validateBoosterRules(any()) } returns null
        coEvery { vaccinationRepository.updateBoosterRule(any(), any()) } just Runs
    }

    @Test
    fun `Booster rules check skipped less than a day`() = runBlockingTest {
        val time = Instant.parse("2021-01-01T00:00:00.000Z")
        every { covidCertificateSettings.lastDccBoosterCheck } returns mockFlowPreference(time)
        every { timeStamper.nowUTC } returns time

        service().checkBoosterNotification()

        verify(exactly = 0) {
            personCertificatesProvider.personCertificates
        }
    }

    @Test
    fun `Non vaccinated persons are not checked`() = runBlockingTest {
        val pIdentifier = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-10-10",
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized"
        )

        val recoveryCertificate = mockk<RecoveryCertificate>().apply {
            every { personIdentifier } returns pIdentifier
        }
        val personCertificate = PersonCertificates(certificates = listOf(recoveryCertificate))
        every { personCertificatesProvider.personCertificates } returns flowOf(setOf(personCertificate))
        every { vaccinationRepository.vaccinationInfos } returns flowOf(setOf())

        service().checkBoosterNotification()

        coVerify(exactly = 0) {
            dccBoosterRulesValidator.validateBoosterRules(any())
        }
    }

    @Test
    fun `Vaccinated persons are checked`() = runBlockingTest {
        val pIdentifier = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-10-10",
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized"
        )

        val vaccinationCertificate = mockk<VaccinationCertificate>().apply {
            every { personIdentifier } returns pIdentifier
        }
        val personCertificate = PersonCertificates(certificates = listOf(vaccinationCertificate))

        val vaccinatedPerson = mockk<VaccinatedPerson>().apply {
            every { identifier } returns pIdentifier
            every { data } returns VaccinatedPersonData(vaccinations = emptySet())
        }
        every { personCertificatesProvider.personCertificates } returns flowOf(setOf(personCertificate))
        every { vaccinationRepository.vaccinationInfos } returns flowOf(setOf(vaccinatedPerson))

        service().checkBoosterNotification()

        coVerifySequence {
            vaccinationRepository.vaccinationInfos
            dccBoosterRulesValidator.validateBoosterRules(any())
            vaccinationRepository.updateBoosterRule(any(), any())
        }
    }

    private fun service() = BoosterNotificationService(
        boosterNotification = boosterNotification,
        personCertificatesProvider = personCertificatesProvider,
        covidCertificateSettings = covidCertificateSettings,
        dccBoosterRulesValidator = dccBoosterRulesValidator,
        vaccinationRepository = vaccinationRepository,
        timeStamper = timeStamper
    )
}
