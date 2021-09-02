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
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
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

    @Test
    fun `User isn't notified when rule did not change`() = runBlockingTest {
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
            every { data } returns VaccinatedPersonData(
                vaccinations = emptySet(),
                lastSeenBoosterRuleIdentifier = "BNR-DE-416"
            )
        }
        every { personCertificatesProvider.personCertificates } returns flowOf(setOf(personCertificate))
        every { vaccinationRepository.vaccinationInfos } returns flowOf(setOf(vaccinatedPerson))
        coEvery { dccBoosterRulesValidator.validateBoosterRules(any()) } returns mockk<DccValidationRule>().apply {
            every { identifier } returns "BNR-DE-416"
        }

        service().checkBoosterNotification()
        coVerify(exactly = 0) {
            boosterNotification.showBoosterNotification(any())
            vaccinationRepository.updateBoosterNotifiedAt(any(), any())
        }
    }

    @Test
    fun `User isn't notified when rule stays null`() = runBlockingTest {
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

        coVerify(exactly = 0) {
            boosterNotification.showBoosterNotification(any())
            vaccinationRepository.updateBoosterNotifiedAt(any(), any())
        }
    }

    @Test
    fun `User is notified when rule changes`() = runBlockingTest {
        val pIdentifier = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-10-10",
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized"
        )

        val vaccinationCertificate = mockk<VaccinationCertificate>()
            .apply { every { personIdentifier } returns pIdentifier }
        val personCertificate = PersonCertificates(certificates = listOf(vaccinationCertificate))

        val vaccinatedPerson = mockk<VaccinatedPerson>().apply {
            every { identifier } returns pIdentifier
            every { data } returns VaccinatedPersonData(
                vaccinations = emptySet(),
                lastSeenBoosterRuleIdentifier = "BNR-DE-416"
            )
        }
        every { personCertificatesProvider.personCertificates } returns flowOf(setOf(personCertificate))
        every { vaccinationRepository.vaccinationInfos } returns flowOf(setOf(vaccinatedPerson))
        coEvery { dccBoosterRulesValidator.validateBoosterRules(any()) } returns
            mockk<DccValidationRule>().apply { every { identifier } returns "BNR-DE-410" }

        service().checkBoosterNotification()
        coVerify {
            boosterNotification.showBoosterNotification(any())
            vaccinationRepository.updateBoosterNotifiedAt(any(), any())
        }
    }

    @Test
    fun `Multiple persons are notified when they are eligible`() = runBlockingTest {
        val pIdentifier1 = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-10-10",
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized"
        )

        val vaccinationCertificate1 = mockk<VaccinationCertificate>()
            .apply { every { personIdentifier } returns pIdentifier1 }
        val personCertificate1 = PersonCertificates(certificates = listOf(vaccinationCertificate1))

        val vaccinatedPerson1 = mockk<VaccinatedPerson>().apply {
            every { identifier } returns pIdentifier1
            every { data } returns VaccinatedPersonData(
                vaccinations = emptySet(),
                lastSeenBoosterRuleIdentifier = "BNR-DE-416"
            )
        }

        val pIdentifier2 = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-10-10",
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized"
        )

        val vaccinationCertificate2 = mockk<VaccinationCertificate>()
            .apply { every { personIdentifier } returns pIdentifier2 }
        val personCertificate2 = PersonCertificates(certificates = listOf(vaccinationCertificate2))

        val vaccinatedPerson2 = mockk<VaccinatedPerson>().apply {
            every { identifier } returns pIdentifier2
            every { data } returns VaccinatedPersonData(
                vaccinations = emptySet(),
                lastSeenBoosterRuleIdentifier = "BNR-DE-400"
            )
        }

        every { personCertificatesProvider.personCertificates } returns flowOf(
            setOf(
                personCertificate1,
                personCertificate2
            )
        )
        every { vaccinationRepository.vaccinationInfos } returns flowOf(
            setOf(vaccinatedPerson1, vaccinatedPerson2)
        )
        coEvery { dccBoosterRulesValidator.validateBoosterRules(any()) } returns
            mockk<DccValidationRule>().apply { every { identifier } returns "BNR-DE-500" }

        service().checkBoosterNotification()
        coVerify(exactly = 2) {
            boosterNotification.showBoosterNotification(any())
            vaccinationRepository.updateBoosterNotifiedAt(any(), any())
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
