package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.lifecycle.SavedStateHandle
import de.rki.coronawarnapp.ccl.dccadmission.calculation.DccAdmissionCheckScenariosCalculation
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CclJsonFunctions
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.MigrationCheck
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosSharedViewModel
import de.rki.coronawarnapp.covidcertificate.person.ui.dccAdmissionCheckScenarios
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.AdmissionTileProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class PersonOverviewViewModelTest : BaseTest() {
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var refreshResult: TestCertificateRepository.RefreshResult
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var admissionCheckScenariosCalculation: DccAdmissionCheckScenariosCalculation
    @MockK lateinit var admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel
    @MockK lateinit var cclJsonFunctions: CclJsonFunctions
    @MockK lateinit var admissionTileProvider: AdmissionTileProvider
    @MockK lateinit var migrationCheck: MigrationCheck
    @MockK lateinit var onboardingSettings: OnboardingSettings
    private val mapper = SerializationModule.jacksonBaseMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, true)
        mockkStatic("de.rki.coronawarnapp.contactdiary.util.ContactDiaryExtensionsKt")

        coEvery { testCertificateRepository.refresh(any()) } returns setOf(refreshResult)
        every { personCertificatesProvider.personCertificates } returns flowOf(
            setOf(
                PersonCertificates(
                    certificates = listOf(),
                    isCwaUser = true,
                    dccWalletInfo = null
                )
            )
        )
        every { migrationCheck.shouldShowMigrationInfo(any()) } returns false
        every { refreshResult.error } returns null
        every { testCertificateRepository.certificates } returns flowOf(setOf())
        every { valueSetsRepository.triggerUpdateValueSet(any()) } just Runs
        every { admissionTileProvider.admissionTile } returns flowOf(
            AdmissionTileProvider.AdmissionTile(
                visible = true,
                title = "Status anzeigen für folgendes Bundesland:",
                subtitle = "Bundesweit"
            )
        )
        coEvery { admissionScenariosSharedViewModel.setAdmissionScenarios(any()) } just Runs
        every { onboardingSettings.exportAllOnboardingDone } returns flowOf(true)
    }

    @Test
    fun `refreshCertificate causes an error dialog event`() {
        val error = mockk<Exception>()
        every { refreshResult.error } returns error

        instance.apply {
            refreshCertificate(TestCertificateContainerId("Identifier"))
            events.getOrAwaitValue() shouldBe ShowRefreshErrorDialog(error)
        }
    }

    @Test
    fun `refreshCertificate triggers refresh operation in repo`() {
        instance.refreshCertificate(TestCertificateContainerId("Identifier"))
        coVerify { testCertificateRepository.refresh(any()) }
    }

    @Test
    fun `deleteTestCertificate deletes certificates from repo`() {
        coEvery { testCertificateRepository.deleteCertificate(any()) } returns mockk()
        instance.apply {
            deleteTestCertificate(TestCertificateContainerId("Identifier"))
        }

        coVerify { testCertificateRepository.deleteCertificate(any()) }
    }

    @Test
    fun `Sorting - List has pending certificate`() {
        every { testCertificateRepository.certificates } returns flowOf(
            setOf(PersonCertificatesData.mockTestCertificateWrapper)
        )
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithPending
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                    }
                }.run { flowOf(this.toSet()) }

        instance.uiState.apply {
            getOrAwaitValue().apply {
                this shouldBe PersonOverviewViewModel.UiState.Loading
            }
            getOrAwaitValue().apply {
                this as PersonOverviewViewModel.UiState.Done
                (personCertificates[0] as CovidTestCertificatePendingCard.Item).apply {
                    certificate.containerId shouldBe TestCertificateContainerId(
                        "testCertificateContainerId"
                    )
                }
                (personCertificates[1] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Zeebee"
                }
                (personCertificates[2] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Andrea Schneider"
                }
            }
        }
    }

    @Test
    fun `Sorting - List has pending & updating certificate`() {
        every { testCertificateRepository.certificates } returns flowOf(
            setOf(PersonCertificatesData.mockTestCertificateWrapper)
        )
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithUpdating
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                    }
                }.run { flowOf(this.toSet()) }

        instance.uiState.apply {
            getOrAwaitValue().apply {
                this shouldBe PersonOverviewViewModel.UiState.Loading
            }
            getOrAwaitValue().apply {
                this as PersonOverviewViewModel.UiState.Done
                (personCertificates[0] as CovidTestCertificatePendingCard.Item).apply {
                    certificate.containerId shouldBe TestCertificateContainerId(
                        "testCertificateContainerId"
                    )
                }
                (personCertificates[1] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Zeebee"
                }
                (personCertificates[2] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Andrea Schneider"
                }
            }
        }
    }

    @Test
    fun `Sorting - List has no CWA user`() {
        every { testCertificateRepository.certificates } returns flowOf(setOf())
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithoutCwaUser
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                    }
                }.run { flowOf(this.toSet()) }

        instance.uiState.apply {
            getOrAwaitValue().apply {
                this shouldBe PersonOverviewViewModel.UiState.Loading
            }
            getOrAwaitValue().apply {
                this as PersonOverviewViewModel.UiState.Done
                (personCertificates[0] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Andrea Schneider"
                }
                (personCertificates[1] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Erika Musterfrau"
                }
                (personCertificates[2] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Max Mustermann"
                }
            }
        }
    }

    @Test
    fun `Sorting - List has CWA user`() {
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithCwaUser
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                    }
                }.run { flowOf(this.toSet()) }

        instance.uiState.apply {
            getOrAwaitValue().apply {
                this shouldBe PersonOverviewViewModel.UiState.Loading
            }
            getOrAwaitValue().apply {
                this as PersonOverviewViewModel.UiState.Done
                (personCertificates[0] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Zeebee"
                } // CWA user
                (personCertificates[1] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Andrea Schneider"
                }
                (personCertificates[2] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Erika Musterfrau"
                }
                (personCertificates[3] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Max Mustermann"
                }
                (personCertificates[4] as PersonCertificateCard.Item).apply {
                    overviewCertificates[0].cwaCertificate.fullName shouldBe "Zeebee A"
                }
            }
        }
    }

    @Test
    fun `admission tile is visible`() {
        instance.run {
            admissionTile.getOrAwaitValue() shouldBe AdmissionTileProvider.AdmissionTile(
                visible = true,
                title = "Status anzeigen für folgendes Bundesland:",
                subtitle = "Bundesweit"
            )
        }
    }

    @Test
    fun `openAdmissionScenarioScreen - success`() {
        coEvery { admissionCheckScenariosCalculation.getDccAdmissionCheckScenarios(any()) } returns
            dccAdmissionCheckScenarios

        instance.apply {
            openAdmissionScenarioScreen()
            events.getOrAwaitValue() shouldBe OpenAdmissionScenarioScreen
        }
        coVerify {
            admissionCheckScenariosCalculation.getDccAdmissionCheckScenarios(any())
            admissionScenariosSharedViewModel.setAdmissionScenarios(any())
        }
    }

    @Test
    fun `openAdmissionScenarioScreen - error`() {
        val exception = Exception("Crash!")
        coEvery { admissionCheckScenariosCalculation.getDccAdmissionCheckScenarios(any()) } throws exception

        instance.apply {
            openAdmissionScenarioScreen()
            events.getOrAwaitValue() shouldBe ShowAdmissionScenarioError(exception)
        }
        coVerify {
            admissionCheckScenariosCalculation.getDccAdmissionCheckScenarios(any())
        }

        coVerify(exactly = 0) {
            admissionScenariosSharedViewModel.setAdmissionScenarios(any())
        }
    }

    private val instance
        get() = PersonOverviewViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            testCertificateRepository = testCertificateRepository,
            certificatesProvider = personCertificatesProvider,
            appScope = TestScope(),
            format = CclTextFormatter(cclJsonFunctions, mapper),
            admissionScenariosSharedViewModel = admissionScenariosSharedViewModel,
            admissionCheckScenariosCalculation = admissionCheckScenariosCalculation,
            dccAdmissionTileProvider = admissionTileProvider,
            migrationCheck = migrationCheck,
            onboardingSettings = onboardingSettings,
            savedState = SavedStateHandle()
        )
}
