package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
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
import kotlinx.coroutines.flow.flowOf
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
        coEvery { qrCodeGenerator.createQrCode(any(), any(), any(), any(), any()) } returns mockk()
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

    @Test
    fun `Sorting - List has pending certificate`() {
        every { personCertificatesProvider.personCertificates } returns
            flowOf(PersonCertificatesData.certificatesWithPending)
        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as CovidTestCertificatePendingCard.Item).apply { certificate.fullName shouldBe "Max Mustermann" }
            (get(1) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Zeebee" }
            (get(2) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Andrea Schneider" }
        }
    }

    @Test
    fun `Sorting - List has pending & updating certificate`() {
        every { personCertificatesProvider.personCertificates } returns
            flowOf(PersonCertificatesData.certificatesWithUpdating)
        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as CovidTestCertificatePendingCard.Item).apply { certificate.fullName shouldBe "Max Mustermann" }
            (get(1) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Zeebee" }
            (get(2) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Andrea Schneider" }
        }
    }

    @Test
    fun `Sorting - List has no CWA user`() {
        every { personCertificatesProvider.personCertificates } returns
            flowOf(PersonCertificatesData.certificatesWithoutCwaUser)
        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Andrea Schneider" }
            (get(1) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Erika Musterfrau" }
            (get(2) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Max Mustermann" }
        }
    }

    @Test
    fun `Sorting - List has CWA user`() {
        every { personCertificatesProvider.personCertificates } returns
            flowOf(PersonCertificatesData.certificatesWithCwaUser)
        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Zeebee" } // CWA user
            (get(1) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Andrea Schneider" }
            (get(2) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Erika Musterfrau" }
            (get(3) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Max Mustermann" }
            (get(4) as PersonCertificateCard.Item).apply { certificate.fullName shouldBe "Zeebee A" }
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
