package de.rki.coronawarnapp.dccreissuance.ui.consent.acccert

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dccWalletInfoWithReissuance
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceCertificateCard
import de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts.DccReissuanceAccCertsViewModel
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
import java.time.LocalDate

@ExtendWith(InstantExecutorExtension::class)
internal class DccReissuanceAccCertsViewModelTest : BaseTest() {

    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var dccQrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var dccQrCode: DccQrCode
    @MockK lateinit var metadata: DccV1.MetaData
    @MockK lateinit var cwaCertificates: CwaCovidCertificate
    @MockK lateinit var timeStamper: TimeStamper

    private val identifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.10.1982",
        firstNameStandardized = "fN",
        lastNameStandardized = "lN"
    )

    private val vaccinationCertificate = mockk<VaccinationCertificate>().apply {
        every { personIdentifier } returns identifier
        every { vaccinatedOn } returns LocalDate.parse("2021-10-03")
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { cwaCertificates.personIdentifier } returns identifier
        every { personCertificatesProvider.findPersonByIdentifierCode(any()) } returns flowOf(
            PersonCertificates(
                certificates = listOf(vaccinationCertificate),
                dccWalletInfo = dccWalletInfoWithReissuance
            )
        )

        every { metadata.personIdentifier } returns identifier

        coEvery { dccQrCodeExtractor.extract(any(), any()) } returns dccQrCode.apply {
            every { data } returns mockk<DccData<out DccV1.MetaData>>().apply {
                every { certificate } returns metadata
                every { header.expiresAt } returns Instant.EPOCH
            }
        }
        every { timeStamper.nowUTC } returns Instant.EPOCH
    }

    @Test
    fun `getState works`() {
        viewModel().certificatesLiveData.getOrAwaitValue() shouldBe listOf(
            DccReissuanceCertificateCard.Item(metadata)
        )
    }

    private fun viewModel() = DccReissuanceAccCertsViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        personCertificatesProvider = personCertificatesProvider,
        personIdentifierCode = "code",
        dccQrCodeExtractor = dccQrCodeExtractor,
        timeStamper = timeStamper
    )
}
