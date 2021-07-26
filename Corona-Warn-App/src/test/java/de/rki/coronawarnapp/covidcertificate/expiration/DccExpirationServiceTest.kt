package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class DccExpirationServiceTest : BaseTest() {
    @MockK lateinit var expirationNotification: DccExpirationNotification
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var recoveryRepository: RecoveryCertificateRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var vaccinatedPerson: VaccinatedPerson
    @MockK lateinit var vaccinationCertificate: VaccinationCertificate
    private val vaccinationContainerId = VaccinationCertificateContainerId("vac")

    @MockK lateinit var recoveryCertificateWrapper: RecoveryCertificateWrapper
    @MockK lateinit var recoveryCertificate: RecoveryCertificate
    private val recoverContainerId = RecoveryCertificateContainerId("rec")

    private val lastDccStateBackgroundCheck = mockFlowPreference(Instant.EPOCH)
    private val nowUtc = Instant.EPOCH.plus(Duration.standardDays(7))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUtc

        every { covidCertificateSettings.lastDccStateBackgroundCheck } returns lastDccStateBackgroundCheck

        expirationNotification.apply {
            coEvery { showExpiredNotification(any()) } returns true
            coEvery { showExpiresSoonNotification(any()) } returns true
        }

        vaccinationRepository.apply {
            every { vaccinationInfos } returns flowOf(setOf(vaccinatedPerson))
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
        }
        every { vaccinatedPerson.vaccinationCertificates } returns setOf(vaccinationCertificate)
        vaccinationCertificate.apply {
            every { getState() } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns vaccinationContainerId
            every { notifiedExpiresSoonAt } returns null
            every { notifiedExpiredAt } returns null
        }

        recoveryRepository.apply {
            coEvery { certificates } returns flowOf(setOf(recoveryCertificateWrapper))
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
        }
        every { recoveryCertificateWrapper.recoveryCertificate } returns recoveryCertificate
        recoveryCertificate.apply {
            every { getState() } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns recoverContainerId
            every { notifiedExpiresSoonAt } returns null
            every { notifiedExpiredAt } returns null
        }
    }

    fun createInstance() = DccExpirationNotificationService(
        dscCheckNotification = expirationNotification,
        vaccinationRepository = vaccinationRepository,
        recoveryRepository = recoveryRepository,
        covidCertificateSettings = covidCertificateSettings,
        timeStamper = timeStamper,
    )

    @Test
    fun `only once per day`() = runBlockingTest {
        lastDccStateBackgroundCheck.update { timeStamper.nowUTC }
        createInstance().apply {
            verify {
                vaccinationRepository wasNot Called
                recoveryRepository wasNot Called
                expirationNotification wasNot Called
            }
        }
    }

    @Test
    fun `no certificates at all`() = runBlockingTest {
        every { vaccinationRepository.vaccinationInfos } returns flowOf(emptySet())
        every { recoveryRepository.certificates } returns flowOf(emptySet())

        createInstance().showNotificationIfExpired()

        verify {
            vaccinationRepository.vaccinationInfos
            recoveryRepository.certificates
            expirationNotification wasNot Called
        }
    }

    @Test
    fun `certificates that are all valid`() = runBlockingTest {
        createInstance().showNotificationIfExpired()

        verify { expirationNotification wasNot Called }

        coVerify(exactly = 0) {
            vaccinationRepository.setNotifiedState(any(), any(), any())
            recoveryRepository.setNotifiedState(any(), any(), any())
        }
    }

    @Test
    fun `two expired certificates`() = runBlockingTest {
        every { vaccinationCertificate.getState() } returns State.Expired(expiredAt = Instant.EPOCH)
        every { recoveryCertificate.getState() } returns State.Expired(expiredAt = Instant.EPOCH)

        createInstance().showNotificationIfExpired()

        coVerify(exactly = 0) { expirationNotification.showExpiresSoonNotification(any()) }
        coVerify(exactly = 1) { expirationNotification.showExpiredNotification(vaccinationCertificate) }
    }

    @Test
    fun `two soon expiring certificates`() = runBlockingTest {
        every { vaccinationCertificate.getState() } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
        every { recoveryCertificate.getState() } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)

        createInstance().showNotificationIfExpired()

        coVerify(exactly = 1) { expirationNotification.showExpiresSoonNotification(vaccinationCertificate) }
        coVerify(exactly = 0) { expirationNotification.showExpiredNotification(any()) }
    }

    @Test
    fun `one of each`() = runBlockingTest {
        every { vaccinationCertificate.getState() } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
        every { recoveryCertificate.getState() } returns State.Expired(expiredAt = Instant.EPOCH)

        createInstance().showNotificationIfExpired()

        coVerify(exactly = 1) {
            expirationNotification.showExpiresSoonNotification(vaccinationCertificate)
            vaccinationRepository.setNotifiedState(
                containerId = vaccinationContainerId,
                state = State.ExpiringSoon(expiresAt = Instant.EPOCH),
                time = nowUtc,
            )

            expirationNotification.showExpiredNotification(recoveryCertificate)
            recoveryRepository.setNotifiedState(
                containerId = recoverContainerId,
                state = State.Expired(expiredAt = Instant.EPOCH),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one of each but already notified the user`() = runBlockingTest {
        vaccinationCertificate.apply {
            every { getState() } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
            every { notifiedExpiresSoonAt } returns Instant.EPOCH
        }
        recoveryCertificate.apply {
            every { getState() } returns State.Expired(expiredAt = Instant.EPOCH)
            every { notifiedExpiredAt } returns Instant.EPOCH
        }

        createInstance().showNotificationIfExpired()

        coVerify(exactly = 0) {
            expirationNotification.showExpiresSoonNotification(any())
            expirationNotification.showExpiredNotification(any())
        }
    }
}
