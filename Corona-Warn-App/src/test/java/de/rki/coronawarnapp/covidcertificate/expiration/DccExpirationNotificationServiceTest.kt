package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
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

class DccExpirationNotificationServiceTest : BaseTest() {
    @MockK lateinit var expirationNotification: DccExpirationNotification
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var recoveryRepository: RecoveryCertificateRepository
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var vaccinatedPerson: VaccinatedPerson
    @MockK lateinit var vaccinationCertificate: VaccinationCertificate
    private val vaccinationContainerId = VaccinationCertificateContainerId("vac")

    @MockK lateinit var recoveryCertificateWrapper: RecoveryCertificateWrapper
    @MockK lateinit var recoveryCertificate: RecoveryCertificate
    private val recoverContainerId = RecoveryCertificateContainerId("rec")

    @MockK lateinit var testCertificateWrapper: TestCertificateWrapper
    @MockK lateinit var testCertificate: TestCertificate
    private val testContainerId = TestCertificateContainerId("test")

    private val lastDccStateBackgroundCheck = mockFlowPreference(Instant.EPOCH)
    private val nowUtc = Instant.EPOCH.plus(Duration.standardDays(7))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUtc

        every { covidCertificateSettings.lastDccStateBackgroundCheck } returns lastDccStateBackgroundCheck

        expirationNotification.apply {
            coEvery { showNotification(any()) } returns true
        }

        vaccinationRepository.apply {
            every { freshVaccinationInfos } returns flowOf(setOf(vaccinatedPerson))
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
        }
        every { vaccinatedPerson.vaccinationCertificates } returns setOf(vaccinationCertificate)
        vaccinationCertificate.apply {
            every { getState() } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns vaccinationContainerId
            every { notifiedExpiresSoonAt } returns null
            every { notifiedExpiredAt } returns null
            every { notifiedInvalidAt } returns null
        }

        recoveryRepository.apply {
            coEvery { freshCertificates } returns flowOf(setOf(recoveryCertificateWrapper))
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
        }
        every { recoveryCertificateWrapper.recoveryCertificate } returns recoveryCertificate
        recoveryCertificate.apply {
            every { getState() } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns recoverContainerId
            every { notifiedExpiresSoonAt } returns null
            every { notifiedExpiredAt } returns null
            every { notifiedInvalidAt } returns null
        }

        every { testCertificateWrapper.testCertificate } returns testCertificate
        testCertificate.apply {
            every { getState() } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns testContainerId
            every { notifiedExpiresSoonAt } returns null
            every { notifiedExpiredAt } returns null
            every { notifiedInvalidAt } returns null
        }

        testCertificateRepository.apply {
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
            every { certificates } returns flowOf(setOf(testCertificateWrapper))
        }
    }

    fun createInstance() = DccExpirationNotificationService(
        dscCheckNotification = expirationNotification,
        vaccinationRepository = vaccinationRepository,
        recoveryRepository = recoveryRepository,
        covidCertificateSettings = covidCertificateSettings,
        testCertificateRepository = testCertificateRepository,
        timeStamper = timeStamper,
    )

    @Test
    fun `only once per day`() = runBlockingTest {
        lastDccStateBackgroundCheck.update { timeStamper.nowUTC }
        createInstance().apply {
            showNotificationIfStateChanged()

            verify {
                vaccinationRepository wasNot Called
                recoveryRepository wasNot Called
                expirationNotification wasNot Called
            }
        }
    }

    @Test
    fun `check can be enforced`() = runBlockingTest {
        lastDccStateBackgroundCheck.update { timeStamper.nowUTC }
        createInstance().run {
            showNotificationIfStateChanged(ignoreLastCheck = true)

            verify {
                vaccinationRepository.freshVaccinationInfos
                recoveryRepository.freshCertificates
            }
        }
    }

    @Test
    fun `no certificates at all`() = runBlockingTest {
        every { vaccinationRepository.freshVaccinationInfos } returns flowOf(emptySet())
        every { recoveryRepository.freshCertificates } returns flowOf(emptySet())

        createInstance().showNotificationIfStateChanged()

        verify {
            vaccinationRepository.freshVaccinationInfos
            recoveryRepository.freshCertificates
            expirationNotification wasNot Called
        }
    }

    @Test
    fun `certificates that are all valid`() = runBlockingTest {
        createInstance().showNotificationIfStateChanged()

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

        createInstance().showNotificationIfStateChanged()

        coVerify { expirationNotification.showNotification(any()) }
    }

    @Test
    fun `two soon expiring certificates`() = runBlockingTest {
        every { vaccinationCertificate.getState() } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
        every { recoveryCertificate.getState() } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)

        createInstance().showNotificationIfStateChanged()

        coVerify { expirationNotification.showNotification(any()) }
    }

    @Test
    fun `one of each`() = runBlockingTest {
        every { vaccinationCertificate.getState() } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
        every { recoveryCertificate.getState() } returns State.Expired(expiredAt = Instant.EPOCH)

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 2) { expirationNotification.showNotification(any()) }

        coVerify(exactly = 1) {
            vaccinationRepository.setNotifiedState(
                containerId = vaccinationContainerId,
                state = State.ExpiringSoon(expiresAt = Instant.EPOCH),
                time = nowUtc,
            )

            recoveryRepository.setNotifiedState(
                containerId = recoverContainerId,
                state = State.Expired(expiredAt = Instant.EPOCH),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one invalid each - one notification`() = runBlockingTest {
        every { vaccinationCertificate.getState() } returns State.Invalid()
        every { recoveryCertificate.getState() } returns State.Invalid()
        every { testCertificate.getState() } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            expirationNotification.showNotification(any())
            vaccinationRepository.setNotifiedState(
                containerId = vaccinationContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )
        }

        coVerify(exactly = 0) {
            recoveryRepository.setNotifiedState(
                containerId = recoverContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )

            testCertificateRepository.setNotifiedState(
                containerId = testContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one invalid test certificate`() = runBlockingTest {
        every { testCertificate.getState() } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            expirationNotification.showNotification(any())
            testCertificateRepository.setNotifiedState(
                containerId = testContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one invalid vaccination certificate`() = runBlockingTest {
        every { vaccinationCertificate.getState() } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            expirationNotification.showNotification(any())
            vaccinationRepository.setNotifiedState(
                containerId = vaccinationContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one invalid recovery certificate`() = runBlockingTest {
        every { recoveryCertificate.getState() } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            expirationNotification.showNotification(any())
            recoveryRepository.setNotifiedState(
                containerId = recoverContainerId,
                state = State.Invalid(),
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

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 0) {
            expirationNotification.showNotification(any())
        }
    }
}
