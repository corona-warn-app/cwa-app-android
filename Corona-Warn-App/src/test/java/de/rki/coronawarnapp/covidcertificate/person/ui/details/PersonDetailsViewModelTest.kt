package de.rki.coronawarnapp.covidcertificate.person.ui.details

import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CclJsonFunctions
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.toLocalDateUserTz
import de.rki.coronawarnapp.util.ui.observeOnce
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
import java.time.LocalDate

@ExtendWith(InstantExecutorExtension::class)
class PersonDetailsViewModelTest : BaseTest() {
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings
    @MockK lateinit var dccValidationRepository: DccValidationRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var recycledCertificatesProvider: RecycledCertificatesProvider
    @MockK lateinit var viewModel: PersonDetailsViewModel
    @MockK private lateinit var cclJsonFunctions: CclJsonFunctions
    private val mapper = SerializationModule.jacksonBaseMapper

    private val vcContainerId = VaccinationCertificateContainerId("1")
    private val tcsContainerId = TestCertificateContainerId("2")
    private val rcContainerId = RecoveryCertificateContainerId("3")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, true)
    }

    @Test
    fun `Navigates back when no person found`() {
        val vaccCert1 = mockVaccinationCertificate(1)
        val vaccCert2 = mockVaccinationCertificate(2)
        every { personCertificatesProvider.personCertificates } returns flowOf(
            setOf(
                PersonCertificates(
                    listOf(
                        mockTestCertificate(),
                        vaccCert1,
                        vaccCert2,
                        mockRecoveryCertificate()
                    )
                )
            )
        )

        personDetailsViewModel("personIdentifierCode").apply {
            uiState.observeOnce { } // To trigger the flow
            events.getOrAwaitValue() shouldBe Back
        }
    }

    @Test
    fun `Ui state and navigation`() {
        val vaccCert1 = mockVaccinationCertificate(1)
        val vaccCert2 = mockVaccinationCertificate(2)
        every { personCertificatesProvider.personCertificates } returns flowOf(
            setOf(
                PersonCertificates(
                    listOf(
                        mockTestCertificate(),
                        vaccCert1,
                        vaccCert2,
                        mockRecoveryCertificate()
                    )
                )
            )
        )

        coEvery { personCertificatesProvider.setCurrentCwaUser(any()) } just Runs

        every { timeStamper.nowUTC } returns Instant.EPOCH
        personDetailsViewModel(certificatePersonIdentifier.groupingKey)
            .apply {
                uiState.getOrAwaitValue().also {

                    it.name shouldBe vaccCert1.fullName

                    it.certificateItems.run {
                        (get(0) as CwaUserCard.Item).apply {
                            onSwitch(true)
                            coVerify { personCertificatesProvider.setCurrentCwaUser(any()) }
                        }

                        (get(1) as RecoveryCertificateCard.Item).apply {
                            onClick()
                            events.getOrAwaitValue() shouldBe OpenRecoveryCertificateDetails(
                                rcContainerId,
                                colorShade
                            )
                        }

                        (get(2) as TestCertificateCard.Item).apply {
                            onClick()
                            events.getOrAwaitValue() shouldBe OpenTestCertificateDetails(
                                tcsContainerId,
                                PersonColorShade.COLOR_INVALID
                            )
                        }

                        (get(3) as VaccinationCertificateCard.Item).apply {
                            onClick()
                            events.getOrAwaitValue() shouldBe OpenVaccinationCertificateDetails(
                                vcContainerId,
                                PersonColorShade.COLOR_INVALID
                            )
                        }

                        (get(4) as VaccinationCertificateCard.Item).apply {
                            onClick()
                            events.getOrAwaitValue() shouldBe OpenVaccinationCertificateDetails(
                                vcContainerId,
                                PersonColorShade.COLOR_INVALID
                            )
                        }
                    }
                }
            }
    }

    private fun personDetailsViewModel(personCode: String) = PersonDetailsViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        dccValidationRepository = dccValidationRepository,
        personCertificatesProvider = personCertificatesProvider,
        personCertificatesSettings = personCertificatesSettings,
        recycledCertificatesProvider = recycledCertificatesProvider,
        groupKey = personCode,
        colorShade = PersonColorShade.COLOR_1,
        format = CclTextFormatter(cclJsonFunctions, mapper)
    )

    private fun mockTestCertificate(): TestCertificate = mockk<TestCertificate>().apply {
        every { uniqueCertificateIdentifier } returns "RN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
        every { fullName } returns "Andrea Schneider"
        every { rawCertificate } returns mockk<TestDccV1>().apply {
            every { test } returns mockk<DccV1.TestCertificateData>().apply {
                every { testType } returns "LP6464-4"
                every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
            }
        }
        every { headerExpiresAt } returns Instant.parse("2022-01-01T11:35:00.000Z")
        every { containerId } returns tcsContainerId
        every { testType } returns "PCR-Test"
        every { dateOfBirthFormatted } returns "18.04.1943"
        every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
        every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { personIdentifier } returns certificatePersonIdentifier
        every { isDisplayValid } returns true
        every { state } returns State.Valid(headerExpiresAt)
        every { qrCodeToDisplay } returns CoilQrCode("qrCode")
        every { qrCodeHash } returns "TC"
    }

    private fun mockVaccinationCertificate(number: Int = 1, final: Boolean = false): VaccinationCertificate =
        mockk<VaccinationCertificate>().apply {
            val localDate = Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
            every { uniqueCertificateIdentifier } returns "RN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
            every { fullName } returns "Andrea Schneider"
            every { rawCertificate } returns mockk<VaccinationDccV1>().apply {
                every { vaccination } returns mockk<DccV1.VaccinationData>().apply {
                    every { doseNumber } returns number
                    every { totalSeriesOfDoses } returns 2
                    every { vaccinatedOn } returns localDate
                    every { medicalProductId } returns "EU/1/20/1528"
                }
            }
            every { containerId } returns vcContainerId
            every { vaccinatedOn } returns localDate
            every { personIdentifier } returns certificatePersonIdentifier
            every { medicalProductName } returns "EU/1/20/1528"
            every { doseNumber } returns number
            every { totalSeriesOfDoses } returns 2
            every { isSeriesCompletingShot } returns final
            every { headerIssuedAt } returns Instant.EPOCH
            every { isDisplayValid } returns true
            every { state } returns State.Valid(expiresAt = Instant.parse("2022-01-01T11:35:00.000Z"))
            every { qrCodeToDisplay } returns CoilQrCode("qrCode")
            every { qrCodeHash } returns "VC$number"
        }

    private fun mockRecoveryCertificate(): RecoveryCertificate =
        mockk<RecoveryCertificate>().apply {
            every { uniqueCertificateIdentifier } returns "RN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
            every { personIdentifier } returns certificatePersonIdentifier
            every { qrCodeToDisplay } returns CoilQrCode("qrCode")
            every { containerId } returns rcContainerId
            every { fullName } returns "Andrea Schneider"
            every { isDisplayValid } returns true
            every { testedPositiveOn } returns LocalDate.now()
            every { rawCertificate } returns mockk<RecoveryDccV1>().apply {
                every { recovery } returns mockk<DccV1.RecoveryCertificateData>().apply {
                    every { validFrom } returns LocalDate.now()
                }
            }
            every { state } returns State.Valid(expiresAt = Instant.parse("2022-01-01T11:35:00.000Z"))
            every { qrCodeHash } returns "RC"
        }

    private val certificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.01.2020",
        firstNameStandardized = "firstNameStandardized",
        lastNameStandardized = "lastNameStandardized",
    )
}
