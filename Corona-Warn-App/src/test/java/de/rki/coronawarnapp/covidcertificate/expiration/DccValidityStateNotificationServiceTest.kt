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
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateWrapper
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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant

class DccValidityStateNotificationServiceTest : BaseTest() {
    @MockK lateinit var dccValidityStateNotification: DccValidityStateNotification
    @MockK lateinit var vaccinationCertificateRepository: VaccinationCertificateRepository
    @MockK lateinit var recoveryRepository: RecoveryCertificateRepository
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var vaccinationCertificateWrapper: VaccinationCertificateWrapper
    @MockK lateinit var vaccinationCertificate: VaccinationCertificate
    private val vaccinationContainerId = VaccinationCertificateContainerId("vac")

    @MockK lateinit var recoveryCertificateWrapper: RecoveryCertificateWrapper
    @MockK lateinit var recoveryCertificate: RecoveryCertificate
    private val recoverContainerId = RecoveryCertificateContainerId("rec")

    @MockK lateinit var testCertificateWrapper: TestCertificateWrapper
    @MockK lateinit var testCertificate: TestCertificate
    private val testContainerId = TestCertificateContainerId("test")

    private val nowUtc = Instant.EPOCH.plus(Duration.ofDays(7))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUtc

        every { covidCertificateSettings.lastDccStateBackgroundCheck } returns flowOf(Instant.EPOCH)
        coEvery { covidCertificateSettings.updateLastDccStateBackgroundCheck(any()) } just Runs

        dccValidityStateNotification.apply {
            coEvery { showNotification(any()) } returns true
        }

        vaccinationCertificateRepository.apply {
            every { certificates } returns flowOf(setOf(vaccinationCertificateWrapper))
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
        }

        every { vaccinationCertificateWrapper.vaccinationCertificate } returns vaccinationCertificate

        vaccinationCertificate.apply {
            every { state } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns vaccinationContainerId
            every { notifiedInvalidAt } returns null
            every { notifiedBlockedAt } returns null
        }

        recoveryRepository.apply {
            coEvery { certificates } returns flowOf(setOf(recoveryCertificateWrapper))
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
        }
        every { recoveryCertificateWrapper.recoveryCertificate } returns recoveryCertificate
        recoveryCertificate.apply {
            every { state } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns recoverContainerId
            every { notifiedInvalidAt } returns null
            every { notifiedBlockedAt } returns null
        }

        every { testCertificateWrapper.testCertificate } returns testCertificate
        testCertificate.apply {
            every { state } returns State.Valid(expiresAt = Instant.EPOCH)
            every { containerId } returns testContainerId
            every { notifiedInvalidAt } returns null
            every { notifiedBlockedAt } returns null
        }

        testCertificateRepository.apply {
            coEvery { setNotifiedState(any(), any(), any()) } just Runs
            every { certificates } returns flowOf(setOf(testCertificateWrapper))
        }
    }

    fun createInstance() = DccValidityStateNotificationService(
        stateNotification = dccValidityStateNotification,
        vcRepo = vaccinationCertificateRepository,
        rcRepo = recoveryRepository,
        covidCertificateSettings = covidCertificateSettings,
        tcRepo = testCertificateRepository,
        timeStamper = timeStamper,
    )

    @Test
    fun `only once per day`() = runTest {
        every { covidCertificateSettings.lastDccStateBackgroundCheck } returns flowOf(timeStamper.nowUTC)
        createInstance().apply {
            showNotificationIfStateChanged()

            verify {
                vaccinationCertificateRepository wasNot Called
                recoveryRepository wasNot Called
                dccValidityStateNotification wasNot Called
            }
        }
    }

    @Test
    fun `check can be enforced`() = runTest {
        every { covidCertificateSettings.lastDccStateBackgroundCheck } returns flowOf(timeStamper.nowUTC)
        createInstance().run {
            showNotificationIfStateChanged(forceCheck = true)

            verify {
                vaccinationCertificateRepository.certificates
                recoveryRepository.certificates
            }
        }
    }

    @Test
    fun `no certificates at all`() = runTest {
        every { vaccinationCertificateRepository.certificates } returns flowOf(emptySet())
        every { recoveryRepository.certificates } returns flowOf(emptySet())

        createInstance().showNotificationIfStateChanged()

        verify {
            vaccinationCertificateRepository.certificates
            recoveryRepository.certificates
            dccValidityStateNotification wasNot Called
        }
    }

    @Test
    fun `certificates that are all valid`() = runTest {
        createInstance().showNotificationIfStateChanged()

        verify { dccValidityStateNotification wasNot Called }

        coVerify(exactly = 0) {
            vaccinationCertificateRepository.setNotifiedState(any(), any(), any())
            recoveryRepository.setNotifiedState(any(), any(), any())
        }
    }

    @Test
    fun `two expired certificates`() = runTest {
        every { vaccinationCertificate.state } returns State.Expired(expiredAt = Instant.EPOCH)
        every { recoveryCertificate.state } returns State.Expired(expiredAt = Instant.EPOCH)

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 0) { dccValidityStateNotification.showNotification(any()) }
    }

    @Test
    fun `two soon expiring certificates`() = runTest {
        every { vaccinationCertificate.state } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
        every { recoveryCertificate.state } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 0) { dccValidityStateNotification.showNotification(any()) }
    }

    @Test
    fun `one of each`() = runTest {
        every { vaccinationCertificate.state } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
        every { recoveryCertificate.state } returns State.Expired(expiredAt = Instant.EPOCH)
        every { testCertificate.state } returns State.Blocked

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) { dccValidityStateNotification.showNotification(any()) }

        coVerify {
            testCertificateRepository.setNotifiedState(
                containerId = testContainerId,
                state = State.Blocked,
                time = nowUtc,
            )
        }

        coVerify(exactly = 0) {
            vaccinationCertificateRepository.setNotifiedState(
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
    fun `one invalid each - one notification`() = runTest {
        every { vaccinationCertificate.state } returns State.Invalid()
        every { recoveryCertificate.state } returns State.Invalid()
        every { testCertificate.state } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            vaccinationCertificateRepository.setNotifiedState(
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
    fun `one invalid test certificate`() = runTest {
        every { testCertificate.state } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            testCertificateRepository.setNotifiedState(
                containerId = testContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one invalid vaccination certificate`() = runTest {
        every { vaccinationCertificate.state } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            vaccinationCertificateRepository.setNotifiedState(
                containerId = vaccinationContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one invalid recovery certificate`() = runTest {
        every { recoveryCertificate.state } returns State.Invalid()

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            recoveryRepository.setNotifiedState(
                containerId = recoverContainerId,
                state = State.Invalid(),
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one blocked each - one notification`() = runTest {
        every { vaccinationCertificate.state } returns State.Blocked
        every { recoveryCertificate.state } returns State.Blocked
        every { testCertificate.state } returns State.Blocked

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            vaccinationCertificateRepository.setNotifiedState(
                containerId = vaccinationContainerId,
                state = State.Blocked,
                time = nowUtc,
            )
        }

        coVerify(exactly = 0) {
            recoveryRepository.setNotifiedState(
                containerId = recoverContainerId,
                state = State.Blocked,
                time = nowUtc,
            )

            testCertificateRepository.setNotifiedState(
                containerId = testContainerId,
                state = State.Blocked,
                time = nowUtc,
            )
        }
    }

    @Test
    fun `blocked test certificate notification`() = runTest {
        every { testCertificate.state } returns State.Blocked

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            testCertificateRepository.setNotifiedState(
                containerId = testContainerId,
                state = State.Blocked,
                time = nowUtc,
            )
        }
    }

    @Test
    fun `blocked vaccination certificate notification`() = runTest {
        every { vaccinationCertificate.state } returns State.Blocked

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            vaccinationCertificateRepository.setNotifiedState(
                containerId = vaccinationContainerId,
                state = State.Blocked,
                time = nowUtc,
            )
        }
    }

    @Test
    fun `blocked recovery certificate notification`() = runTest {
        every { recoveryCertificate.state } returns State.Blocked

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 1) {
            dccValidityStateNotification.showNotification(any())
            recoveryRepository.setNotifiedState(
                containerId = recoverContainerId,
                state = State.Blocked,
                time = nowUtc,
            )
        }
    }

    @Test
    fun `one of each but already notified the user`() = runTest {
        vaccinationCertificate.apply {
            every { state } returns State.ExpiringSoon(expiresAt = Instant.EPOCH)
        }
        recoveryCertificate.apply {
            every { state } returns State.Expired(expiredAt = Instant.EPOCH)
        }

        createInstance().showNotificationIfStateChanged()

        coVerify(exactly = 0) {
            dccValidityStateNotification.showNotification(any())
        }
    }
}
