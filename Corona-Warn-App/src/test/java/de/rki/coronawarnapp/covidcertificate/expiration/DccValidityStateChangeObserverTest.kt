package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider.CertificateContainer
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Expired
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.ExpiringSoon
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Recycled
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Valid
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

class DccValidityStateChangeObserverTest : BaseTest() {

    @RelaxedMockK lateinit var dccValidityStateNotificationService: DccValidityStateNotificationService
    @MockK lateinit var certificateProvider: CertificateProvider

    private lateinit var certificateContainerFlow: MutableStateFlow<CertificateContainer>

    private val certValid = createCert(Valid(Instant.EPOCH))
    private val certInvalid = createCert(Invalid())
    private val certExpiringSoon = createCert(ExpiringSoon(Instant.EPOCH))
    private val certExpired = createCert(Expired(Instant.EPOCH))
    private val certBlocked = createCert(Blocked)
    private val certRevoked = createCert(Revoked)
    private val certRecycled = createCert(Recycled)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        certificateContainerFlow = MutableStateFlow(createContainer(emptySet()))
        every { certificateProvider.certificateContainer } returns certificateContainerFlow
    }

    @Test
    fun `does trigger on initial emission`() = runTest2 {
        certificateContainerFlow.value = createContainer(setOf(certExpired))
        createInstance(scope = this).setup()

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does not trigger when empty`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.value = createContainer(emptySet())

        advanceUntilIdle()

        coVerify {
            dccValidityStateNotificationService wasNot Called
        }
    }

    @Test
    fun `does not trigger on Valid`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certValid)) }

        advanceUntilIdle()

        coVerify {
            dccValidityStateNotificationService wasNot Called
        }
    }

    @Test
    fun `does trigger on Invalid`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certInvalid)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on Recycled`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certRecycled)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on Blocked`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certBlocked)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on Revoked`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certRevoked)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on Expired`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpired)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on ExpiringSoon`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpiringSoon)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `only triggers if changed`() = runTest2 {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpired)) }
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpiringSoon)) }

        // Valid does not trigger
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certValid)) }

        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certBlocked)) }

        advanceUntilIdle()

        coVerify(exactly = 3) {
            dccValidityStateNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    private fun createInstance(scope: CoroutineScope) = DccValidityStateChangeObserver(
        appScope = scope,
        certificateProvider = certificateProvider,
        dccValidityStateNotificationService = dccValidityStateNotificationService
    )

    private fun createCert(requiredState: CwaCovidCertificate.State): CwaCovidCertificate = mockk {
        every { state } returns requiredState
        every { qrCodeHash } returns requiredState.type.toSHA256()
    }

    private fun createContainer(certs: Set<CwaCovidCertificate>): CertificateContainer = mockk {
        every { allCwaCertificates } returns certs
    }
}
