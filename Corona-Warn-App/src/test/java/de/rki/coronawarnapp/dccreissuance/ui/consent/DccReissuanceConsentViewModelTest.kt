package de.rki.coronawarnapp.dccreissuance.ui.consent

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dccWalletInfoWithReissuance
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.dccreissuance.core.reissuer.DccReissuer
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
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

@ExtendWith(InstantExecutorExtension::class)
internal class DccReissuanceConsentViewModelTest : BaseTest() {

    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var dccReissuer: DccReissuer
    @MockK lateinit var dccQrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { personCertificatesProvider.findPersonByIdentifierCode(any()) } returns flowOf(
            PersonCertificates(
                certificates = listOf(),
                dccWalletInfo = dccWalletInfoWithReissuance
            )
        )

        coEvery { dccReissuer.startReissuance(any()) } just Runs
        coEvery { dccQrCodeExtractor.extract(any()) } returns mockk()
        coEvery { personCertificatesSettings.dismissReissuanceBadge(any()) } just Runs
    }

    @Test
    fun `getEvent$Corona_Warn_Corona_Warn_App`() {
    }

    @Test
    fun `getStateLiveData$Corona_Warn_Corona_Warn_App`() {
    }

    @Test
    fun `startReissuance$Corona_Warn_Corona_Warn_App`() {
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
        personIdentifierCode = "code",
        dccReissuer = dccReissuer,
        format = CclTextFormatter(cclJsonFunctions = mockk(), SerializationModule.jacksonBaseMapper),
        dccQrCodeExtractor = dccQrCodeExtractor,
        personCertificatesSettings = personCertificatesSettings
    )
}
