package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
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
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class DccExpirationChangeObserverTest : BaseTest() {

    @RelaxedMockK lateinit var dccExpirationNotificationService: DccExpirationNotificationService
    @MockK lateinit var certificateProvider: CertificateProvider

    private lateinit var certificateContainerFlow: MutableStateFlow<CertificateProvider.CertificateContainer>

    private val certValid = createCert(CwaCovidCertificate.State.Valid(Instant.EPOCH))
    private val certInvalid = createCert(CwaCovidCertificate.State.Invalid())
    private val certExpiringSoon = createCert(CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH))
    private val certExpired = createCert(CwaCovidCertificate.State.Expired(Instant.EPOCH))
    private val certBlocked = createCert(CwaCovidCertificate.State.Blocked)
    private val certRecycled = createCert(CwaCovidCertificate.State.Recycled)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        certificateContainerFlow = MutableStateFlow(createContainer(setOf(certExpired)))
        every { certificateProvider.certificateContainer } returns certificateContainerFlow
    }

    @Test
    fun `drops initial emission`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()

        advanceUntilIdle()

        coVerify {
            dccExpirationNotificationService wasNot Called
        }
    }

    @Test
    fun `does not trigger when empty`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        certificateContainerFlow.value = createContainer(emptySet())

        advanceUntilIdle()

        coVerify {
            dccExpirationNotificationService wasNot Called
        }
    }

    @Test
    fun `does not trigger on Valid`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certValid)) }

        advanceUntilIdle()

        coVerify {
            dccExpirationNotificationService wasNot Called
        }
    }

    @Test
    fun `does trigger on Invalid`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certInvalid)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccExpirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on Recycled`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certRecycled)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccExpirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on Blocked`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certBlocked)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccExpirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on Expired`() = runBlockingTest2(ignoreActive = true) {
        certificateContainerFlow.value = createContainer(emptySet())
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpired)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccExpirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `does trigger on ExpiringSoon`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpiringSoon)) }

        advanceUntilIdle()

        coVerify(exactly = 1) {
            dccExpirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    @Test
    fun `only triggers if changed`() = runBlockingTest2(ignoreActive = true) {
        certificateContainerFlow.value = createContainer(emptySet())
        createInstance(scope = this).setup()
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpired)) }
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certExpiringSoon)) }

        // Valid does not trigger
        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certValid)) }

        certificateContainerFlow.update { createContainer(it.allCwaCertificates.plusElement(certBlocked)) }


        advanceUntilIdle()

        coVerify(exactly = 3) {
            dccExpirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
        }
    }

    private fun createInstance(scope: CoroutineScope) = DccExpirationChangeObserver(
        appScope = scope,
        certificateProvider = certificateProvider,
        dccExpirationNotificationService = dccExpirationNotificationService
    )

    private fun createCert(state: CwaCovidCertificate.State): CwaCovidCertificate = mockk {
        every { getState() } returns state
        every { uniqueCertificateIdentifier } returns state.type
    }

    private fun createContainer(certs: Set<CwaCovidCertificate>): CertificateProvider.CertificateContainer = mockk {
        every { allCwaCertificates } returns certs
    }
}
