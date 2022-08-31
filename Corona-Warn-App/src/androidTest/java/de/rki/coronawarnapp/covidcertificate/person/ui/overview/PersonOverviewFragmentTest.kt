package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosSharedViewModel
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewViewModel.UiState
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.AdmissionTileProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.updateCountBadge
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
                override fun create(
                    admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel,
                    savedStateHandle: SavedStateHandle
                ): PersonOverviewViewModel {
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
    fun capture_fragment_mask_free_with_badge() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(maskFree()))
        takeSelfieWithBottomNavBadge("mask_free_with_badge", R.id.covid_certificates_graph, 1)
    }

    @Test
    @Screenshot
    fun capture_fragment_mask_free_multiline_with_badge() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(maskFreeMultiLine()))
        takeSelfieWithBottomNavBadge("mask_free_multiline_with_badge", R.id.covid_certificates_graph, 1)
    }

    @Test
    @Screenshot
    fun capture_fragment_mask_required_nostatus_with_badge() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(maskReqiredAndNoStatus()))
        takeSelfieWithBottomNavBadge("mask_required_nostatus_with_badge", R.id.covid_certificates_graph, 1)
    }

    @Test
    @Screenshot
    fun capture_fragment_no_mask_info_status_info() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(noMaskInfoStatusInfo()))
        takeSelfieWithBottomNavBadge("no_mask_info_status_info", R.id.covid_certificates_graph, 1)
    }

    @Test
    @Screenshot
    fun capture_fragment_no_mask_info_no_status_info() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(noMaskInfoNoStatusInfo()))
        takeSelfieWithBottomNavBadge("no_mask_info_no_status_info", R.id.covid_certificates_graph, 1)
    }

    @Test
    @Screenshot
    fun capture_fragment_mask_invalid() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(maskInvalidOutdated()))
        takeSelfieWithBottomNavBadge("mask_invalid", R.id.covid_certificates_graph, 1)
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
}

@Module
abstract class PersonOverviewFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun personOverviewFragment(): PersonOverviewFragment
}
