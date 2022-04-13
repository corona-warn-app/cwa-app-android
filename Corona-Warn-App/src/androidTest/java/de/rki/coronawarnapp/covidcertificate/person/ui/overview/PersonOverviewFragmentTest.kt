package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosSharedViewModel
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewViewModel.UiState
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.AdmissionTileProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard.Item.OverviewCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.updateCountBadge
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.createFakeImageLoaderForQrCodes
import testhelpers.launchInMainActivity
import testhelpers.recyclerScrollTo
import testhelpers.selectBottomNavTab
import testhelpers.setupFakeImageLoader
import testhelpers.takeScreenshot
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class PersonOverviewFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: PersonOverviewViewModel

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.covid_certificates_graph)
            setCurrentDestination(R.id.personOverviewFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { uiState } returns MutableLiveData()
            every { admissionTile } returns MutableLiveData(
                AdmissionTileProvider.AdmissionTile(
                    visible = true,
                    title = "Status anzeigen für folgendes Bundesland:",
                    subtitle = "Bundesweit"
                )
            )
        }
        setupFakeImageLoader(
            createFakeImageLoaderForQrCodes()
        )
        setupMockViewModel(
            object : PersonOverviewViewModel.Factory {
                override fun create(admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel): PersonOverviewViewModel {
                    return viewModel
                }
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchInMainActivity<PersonOverviewFragment>(
            testNavHostController = navController
        )
    }

    @Test
    @Screenshot
    fun capture_fragment_empty() {
        every { viewModel.admissionTile } returns MutableLiveData(
            AdmissionTileProvider.AdmissionTile(
                visible = false,
                title = "Status anzeigen für folgendes Bundesland:",
                subtitle = "Berlin"
            )
        )
        takeSelfie("empty")
    }

    @Test
    @Screenshot
    fun capture_fragment_pending() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(listItemWithPendingItem()))
        takeSelfie("pending")
    }

    @Test
    @Screenshot
    fun capture_fragment_updating() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(listItemWithUpdatingItem()))
        takeSelfie("updating")
    }

    @Test
    @Screenshot
    fun capture_fragment_one_person() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(onePersonItem()))
        takeSelfie("one_person")
    }

    @Test
    @Screenshot
    fun capture_fragment_one_person_admission_berlin() {

        every { viewModel.admissionTile } returns MutableLiveData(
            AdmissionTileProvider.AdmissionTile(
                visible = true,
                title = "Status anzeigen für folgendes Bundesland:",
                subtitle = "Berlin"
            )
        )

        every { viewModel.uiState } returns MutableLiveData(UiState.Done(onePersonItem()))
        takeSelfie("one_person_berlin")
    }

    @Test
    @Screenshot
    fun capture_fragment_two_g_plus_certificate() {
        every { viewModel.admissionTile } returns MutableLiveData(
            AdmissionTileProvider.AdmissionTile(
                visible = false,
                title = "",
                subtitle = ""
            )
        )
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(twoGPlusCertificate()))
        takeSelfie("two_g_plus")
    }

    @Test
    @Screenshot
    fun capture_fragment_third_certificate() {
        every { viewModel.admissionTile } returns MutableLiveData(
            AdmissionTileProvider.AdmissionTile(
                visible = false,
                title = "",
                subtitle = ""
            )
        )
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(threeCertificates()))
        takeSelfie("third_certificate")
    }

    @Test
    @Screenshot
    fun capture_fragment_two_g_plus_certificate_with_badge() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(twoGPlusCertificateWithBadge()))
        takeSelfie("two_g_plus_with_badge")
    }

    @Test
    @Screenshot
    fun capture_fragment_one_person_with_badge() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(onePersonItemWithBadgeCount()))
        takeSelfieWithBottomNavBadge("one_person_with_badge", R.id.covid_certificates_graph, 1)
    }

    @Test
    @Screenshot
    fun capture_fragment_many_persons() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(personsItems()))
        takeSelfie("many_persons")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(1))
        takeScreenshot<PersonOverviewFragment>("many_persons_1")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))
        takeScreenshot<PersonOverviewFragment>("many_persons_2")
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    private fun takeSelfieWithBottomNavBadge(suffix: String, @IdRes badgeId: Int, count: Int) {
        val activityScenario = launchInMainActivity<PersonOverviewFragment>(
            testNavHostController = navController
        )
        activityScenario.onActivity {
            it.findViewById<BottomNavigationView>(R.id.fake_bottom_navigation).updateCountBadge(badgeId, count)
        }
        onView(withId(R.id.fake_bottom_navigation)).perform(selectBottomNavTab(R.id.covid_certificates_graph))
        takeScreenshot<PersonOverviewFragment>(suffix)
    }

    private fun takeSelfie(suffix: String) {
        launchInMainActivity<PersonOverviewFragment>(
            testNavHostController = navController
        )
        onView(withId(R.id.fake_bottom_navigation)).perform(selectBottomNavTab(R.id.covid_certificates_graph))
        takeScreenshot<PersonOverviewFragment>(suffix)
    }

    private fun listItemWithPendingItem() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                CovidTestCertificatePendingCard.Item(
                    certificate = mockTestCertificateWrapper(false),
                    onDeleteAction = {},
                    onRetryAction = {},
                )
            )

            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                                else -> "2G Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 0,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun listItemWithUpdatingItem() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                CovidTestCertificatePendingCard.Item(
                    certificate = mockTestCertificateWrapper(true),
                    onDeleteAction = {},
                    onRetryAction = {},
                )
            )

            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                                else -> "2G Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 0,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun personsItems() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockTestCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                                else -> "Test Certificate"
                            }
                        ),
                    ),
                    admissionBadgeText = "3G",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 5,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )

            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockTestCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                                else -> "Test Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "3G",
                    colorShade = PersonColorShade.COLOR_2,
                    badgeCount = 3,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )

            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                                else -> "2G Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "2G",
                    colorShade = PersonColorShade.COLOR_3,
                    badgeCount = 0,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun onePersonItem() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                                else -> "2G Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "2G",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 0,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun onePersonItemWithBadgeCount() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                                else -> "2G Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "2G",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 1,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun twoGPlusCertificate() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                                else -> "2G Certificate"
                            }
                        ),
                        OverviewCertificate(
                            mockTestCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                                else -> "Test Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "2G+",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 0,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun threeCertificates() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "Geimpf"
                                else -> "2G Certificate"
                            }
                        ),
                        OverviewCertificate(
                            mockTestCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "Getestet"
                                else -> "Test Certificate"
                            }
                        ),
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "Genesen"
                                else -> "Recovery Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "2G+",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 0,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun twoGPlusCertificateWithBadge() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                PersonCertificateCard.Item(
                    overviewCertificates = listOf(
                        OverviewCertificate(
                            mockVaccinationCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                                else -> "2G Certificate"
                            }
                        ),
                        OverviewCertificate(
                            mockTestCertificate("Andrea Schneider"),
                            buttonText = when (Locale.getDefault()) {
                                Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                                else -> "Test Certificate"
                            }
                        )
                    ),
                    admissionBadgeText = "2G+",
                    colorShade = PersonColorShade.COLOR_1,
                    badgeCount = 1,
                    onClickAction = { _, _ -> },
                    onCovPassInfoAction = {}
                )
            )
        }

    private fun mockTestCertificate(
        name: String,
        isPending: Boolean = false,
        isUpdating: Boolean = false
    ): TestCertificate = mockk<TestCertificate>().apply {
        every { headerExpiresAt } returns Instant.now().plus(20)
        every { isCertificateRetrievalPending } returns isPending
        every { isUpdatingData } returns isUpdating
        every { fullName } returns name
        every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirthFormatted = "1943-04-18"
        )
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.testCertificate)
        every { isDisplayValid } returns true
        every { sampleCollectedAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { getState() } returns CwaCovidCertificate.State.Valid(headerExpiresAt)
        every { isNew } returns false
    }

    private fun mockTestCertificateWrapper(isUpdating: Boolean) = mockk<TestCertificateWrapper>().apply {
        every { isCertificateRetrievalPending } returns true
        every { isUpdatingData } returns isUpdating
        every { registeredAt } returns Instant.EPOCH
        every { containerId } returns TestCertificateContainerId("testCertificateContainerId")
    }

    private fun mockVaccinationCertificate(name: String): VaccinationCertificate =
        mockk<VaccinationCertificate>().apply {
            every { headerExpiresAt } returns Instant.now().plus(20)
            every { containerId } returns VaccinationCertificateContainerId("2")
            val localDate = Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
            every { fullName } returns name
            every { fullNameFormatted } returns name
            every { doseNumber } returns 2
            every { totalSeriesOfDoses } returns 2
            every { vaccinatedOn } returns localDate.minusDays(15)
            every { personIdentifier } returns CertificatePersonIdentifier(
                firstNameStandardized = "firstNameStandardized",
                lastNameStandardized = "lastNameStandardized",
                dateOfBirthFormatted = "1943-04-18"
            )
            every { isDisplayValid } returns true
            every { getState() } returns CwaCovidCertificate.State.Valid(headerExpiresAt)
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
        }
}

@Module
abstract class PersonOverviewFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun personOverviewFragment(): PersonOverviewFragment
}
