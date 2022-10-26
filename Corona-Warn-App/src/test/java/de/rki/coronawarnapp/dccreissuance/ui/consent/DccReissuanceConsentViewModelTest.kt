package de.rki.coronawarnapp.dccreissuance.ui.consent

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dccWalletInfoWithReissuanceLegacy
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccreissuance.core.reissuer.DccReissuer
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.toLocalDateUtc
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.time.Instant

@ExtendWith(InstantExecutorExtension::class)
internal class DccReissuanceConsentViewModelTest : BaseTest() {

    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var dccReissuer: DccReissuer
    @MockK lateinit var dccQrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings
    @MockK lateinit var dccQrCode: DccQrCode
    @MockK lateinit var metadata: DccV1.MetaData
    @MockK lateinit var cwaCertificates: CwaCovidCertificate
    @MockK lateinit var timeStamper: TimeStamper

    private val identifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.10.1982",
        firstNameStandardized = "fN",
        lastNameStandardized = "lN"
    )

    private val vaccinationCertA = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns identifier
        every { vaccinatedOn } returns Instant.EPOCH.toLocalDateUtc()
        every { hasNotificationBadge } returns false
        every { headerIssuedAt } returns Instant.EPOCH
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { cwaCertificates.personIdentifier } returns identifier
        every { personCertificatesProvider.findPersonByIdentifierCode(any()) } returns flowOf(
            PersonCertificates(
                certificates = listOf(vaccinationCertA),
                dccWalletInfo = dccWalletInfoWithReissuanceLegacy
            )
        )

        coEvery { dccReissuer.startReissuance(any()) } just Runs
        coEvery { dccQrCodeExtractor.extract(any(), any()) } returns dccQrCode.apply {
            every { data } returns mockk<DccData<out DccV1.MetaData>>().apply {
                every { certificate } returns metadata
                every { header.expiresAt } returns Instant.EPOCH
            }
        }
        coEvery { personCertificatesSettings.dismissReissuanceBadge(any()) } just Runs
        every { metadata.personIdentifier } returns identifier
        every { timeStamper.nowUTC } returns Instant.EPOCH
    }

    @Test
    fun `getState works`() {
        viewModel().stateLiveData.getOrAwaitValue() shouldBe DccReissuanceConsentViewModel.State(
            certificateList = listOf(DccReissuanceCertificateCard.Item(metadata)),
            divisionVisible = true,
            listItemsTitle = "",
            title = "Zertifikat ersetzen",
            subtitle = "",
            content = "Langer Text",
            url = "https://www.coronawarn.app/en/faq/#dcc_admission_state",
            accompanyingCertificatesVisible = true
        )
    }

    @Test
    fun `getState no person dismisses the screen`() {
        every { personCertificatesProvider.findPersonByIdentifierCode(any()) } returns flowOf(null)

        viewModel().apply {
            stateLiveData.observeForever {} // Trigger flow
            event.getOrAwaitValue() shouldBe DccReissuanceConsentViewModel.Back
        }
    }

    @Test
    fun `getState no reissuance dismisses the screen`() {
        every { personCertificatesProvider.findPersonByIdentifierCode(any()) } returns flowOf(
            PersonCertificates(
                certificates = listOf(vaccinationCertA),
                dccWalletInfo = null
            )
        )
        viewModel().apply {
            stateLiveData.observeForever {} // Trigger flow
            event.getOrAwaitValue() shouldBe DccReissuanceConsentViewModel.Back
        }
    }

    @Test
    fun `startReissuance calls dccReissuer`() {
        viewModel().apply {
            startReissuance()
            coVerify { dccReissuer.startReissuance(any()) }
            event.getOrAwaitValue() shouldBe DccReissuanceConsentViewModel.ReissuanceSuccess
        }
    }

    @Test
    fun `startReissuance posts an error`() {
        val exception = Exception("Hello World!")
        coEvery { dccReissuer.startReissuance(any()) } throws exception
        viewModel().apply {
            startReissuance()
            coVerify { dccReissuer.startReissuance(any()) }
            event.getOrAwaitValue() shouldBe DccReissuanceConsentViewModel.ReissuanceError(exception)
        }
    }

    @Test
    fun navigateBack() {
        viewModel().apply {
            navigateBack()
            event.getOrAwaitValue() shouldBe DccReissuanceConsentViewModel.Back
        }
    }

    @Test
    fun openPrivacyScreen() {
        viewModel().apply {
            openPrivacyScreen()
            event.getOrAwaitValue() shouldBe DccReissuanceConsentViewModel.OpenPrivacyScreen
        }
    }

    private fun viewModel() = DccReissuanceConsentViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        personCertificatesProvider = personCertificatesProvider,
        groupKey = "code",
        dccReissuer = dccReissuer,
        format = CclTextFormatter(cclJsonFunctions = mockk(), SerializationModule.jacksonBaseMapper),
        dccQrCodeExtractor = dccQrCodeExtractor,
        personCertificatesSettings = personCertificatesSettings,
        timeStamper = timeStamper
    )
}
