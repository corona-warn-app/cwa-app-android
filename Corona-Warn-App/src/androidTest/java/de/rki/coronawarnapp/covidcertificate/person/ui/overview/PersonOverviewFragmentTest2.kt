package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewViewModel.UiState
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.AdmissionTileProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.updateCountBadge
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.createFakeImageLoaderForQrCodes
import testhelpers.launchInMainActivity
import testhelpers.selectBottomNavTab
import testhelpers.setupFakeImageLoader
import testhelpers.takeScreenshot

@HiltAndroidTest
class PersonOverviewFragmentTest2 : BaseUITest() {

    @MockK lateinit var viewModel: PersonOverviewViewModel

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

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
    }

    @Test
    @Screenshot
    fun gStatusIsInvisibleAndMaskIsInvisible() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(gStatusIsInvisibleAndMaskIsInvisibleItem()))
        takeSelfieWithBottomNavBadge("gStatusIsInvisibleAndMaskIsInvisible", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsNullAndMaskIsNull() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(gStatusIsNullAndMaskIsNullItem()))
        takeSelfieWithBottomNavBadge("gStatusIsNullAndMaskIsNull", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsInvisibleAndMaskIsVisible() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(gStatusIsInvisibleAndMaskIsVisibleItem()))
        takeSelfieWithBottomNavBadge("gStatusIsInvisibleAndMaskIsVisible", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsVisibleAndMaskIsVisible() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(gStatusIsVisibleAndMaskIsVisibleItem()))
        takeSelfieWithBottomNavBadge("gStatusIsVisibleAndMaskIsVisible", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsVisibleAndMaskIsInvisible() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(gStatusIsVisibleAndMaskIsInvisibleItem()))
        takeSelfieWithBottomNavBadge("gStatusIsVisibleAndMaskIsInvisible", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsNullAndMaskIsInvisible() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(gStatusIsNullAndMaskIsInvisibleItem()))
        takeSelfieWithBottomNavBadge("gStatusIsNullAndMaskIsInvisible", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsNullAndMaskIsVisible() {
        every { viewModel.uiState } returns MutableLiveData(UiState.Done(gStatusIsNullAndMaskIsVisibleItem()))
        takeSelfieWithBottomNavBadge("gStatusIsNullAndMaskIsVisible", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsVisibleTextEmptyAndMaskIsVisible() {
        every { viewModel.uiState } returns MutableLiveData(
            UiState.Done(
                gStatusIsVisibleTextEmptyAndMaskIsVisibleItem()
            )
        )
        takeSelfieWithBottomNavBadge("gStatusIsVisibleTextEmptyAndMaskIsVisible", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsVisibleAndMaskIsVisibleTextEmpty() {
        every { viewModel.uiState } returns MutableLiveData(
            UiState.Done(
                gStatusIsVisibleAndMaskIsVisibleTextEmptyItem()
            )
        )
        takeSelfieWithBottomNavBadge("gStatusIsVisibleAndMaskIsVisibleTextEmpty", R.id.covid_certificates_graph)
    }

    @Test
    @Screenshot
    fun gStatusIsVisibleTextEmptyAndMaskIsVisibleTextEmpty() {
        every { viewModel.uiState } returns MutableLiveData(
            UiState.Done(
                gStatusIsVisibleTextEmptyAndMaskIsVisibleTextEmptyItem()
            )
        )
        takeSelfieWithBottomNavBadge(
            "gStatusIsVisibleTextEmptyAndMaskIsVisibleTextEmpty",
            R.id.covid_certificates_graph
        )
    }

    private fun takeSelfieWithBottomNavBadge(suffix: String, @IdRes badgeId: Int) {
        val activityScenario = launchInMainActivity<PersonOverviewFragment>(
            testNavHostController = navController
        )
        activityScenario.onActivity {
            it.findViewById<BottomNavigationView>(R.id.fake_bottom_navigation).updateCountBadge(badgeId, 0)
        }
        onView(withId(R.id.fake_bottom_navigation)).perform(selectBottomNavTab(R.id.covid_certificates_graph))
        takeScreenshot<PersonOverviewFragment>(suffix)
    }
}
