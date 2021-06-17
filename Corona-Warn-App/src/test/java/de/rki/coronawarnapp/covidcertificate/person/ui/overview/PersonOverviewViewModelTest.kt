package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class PersonOverviewViewModelTest : BaseTest() {

    @MockK lateinit var qrCodeGenerator: QrCodeGenerator
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var refreshResult: TestCertificateRepository.RefreshResult

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, true)
        coEvery { testCertificateRepository.refresh(any()) } returns setOf(refreshResult)
        every { personCertificatesProvider.personCertificates } returns emptyFlow()
        every { refreshResult.error } returns null
    }

    @Test
    fun `refreshCertificate causes an error dialog event`() {
        val error = mockk<Exception>()
        every { refreshResult.error } returns error

        instance.apply {
            refreshCertificate("Identifier")
            events.getOrAwaitValue() shouldBe ShowRefreshErrorDialog(error)
        }
    }

    @Test
    fun `refreshCertificate triggers refresh operation in repo`() {
        instance.refreshCertificate("Identifier")
        coVerify { testCertificateRepository.refresh(any()) }
    }

    @Test
    fun `deleteTestCertificate deletes certificates from repo`() {
        coEvery { testCertificateRepository.deleteCertificate(any()) } just Runs
        instance.apply {
            deleteTestCertificate("Identifier")
        }

        coEvery { testCertificateRepository.deleteCertificate(any()) }
    }

    @Test
    fun onScanQrCode() {
        instance.apply {
            onScanQrCode()
            events.getOrAwaitValue() shouldBe ScanQrCode
        }
    }

    private val instance
        get() = PersonOverviewViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            testCertificateRepository = testCertificateRepository,
            certificatesProvider = personCertificatesProvider,
            qrCodeGenerator = qrCodeGenerator
        )
}
