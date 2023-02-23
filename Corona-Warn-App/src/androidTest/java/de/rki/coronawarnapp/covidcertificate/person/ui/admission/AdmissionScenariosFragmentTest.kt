package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@HiltAndroidTest
class AdmissionScenariosFragmentTest : BaseUITest() {

    val factory = object : AdmissionScenariosViewModel.Factory {
        override fun create(
            admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel
        ): AdmissionScenariosViewModel {
            return viewModel
        }
    }

    val viewModel = mockk<AdmissionScenariosViewModel>(relaxed = true)

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.covid_certificates_graph)
            setCurrentDestination(R.id.admissionScenariosFragment)
        }
    }

    private val state = AdmissionScenariosViewModel.State(
        title = "Ihr Bundesland",
        scenarios = listOf(
            AdmissionItemCard.Item(
                identifier = "DE",
                title = "Regeln des Bundes",
                subtitle = "Regeln in Ihrem Bundesland können davon abweichen",
                enabled = true
            ) {},

            AdmissionItemCard.Item(
                identifier = "BW",
                title = "Baden-Württemberg",
                subtitle = "",
                enabled = true
            ) {},

            AdmissionItemCard.Item(
                identifier = "BY",
                title = "Bayern",
                subtitle = "",
                enabled = true
            ) {},

            AdmissionItemCard.Item(
                identifier = "BR",
                title = "Berlin",
                subtitle = "",
                enabled = true
            ) {},

            AdmissionItemCard.Item(
                identifier = "BB",
                title = "Brandenburg",
                subtitle = "Für dieses Bundesland liegen momentan keine Regeln vor",
                enabled = false
            ) {},

            AdmissionItemCard.Item(
                identifier = "HB",
                title = "Bremen",
                subtitle = "",
                enabled = true
            ) {},

            AdmissionItemCard.Item(
                identifier = "HH",
                title = "Hamburg",
                subtitle = "",
                enabled = true
            ) {},

            AdmissionItemCard.Item(
                identifier = "HE",
                title = "Hesse",
                subtitle = "Für dieses Bundesland liegen momentan keine Regeln vor",
                enabled = false
            ) {},

            AdmissionItemCard.Item(
                identifier = "MV",
                title = "Mecklenburg-Vorpommern",
                subtitle = "Für dieses Bundesland liegen momentan keine Regeln vor",
                enabled = false
            ) {},

            AdmissionItemCard.Item(
                identifier = "NI",
                title = "Niedersachsen",
                subtitle = "Für dieses Bundesland liegen momentan keine Regeln vor",
                enabled = false
            ) {},
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { viewModel.state } returns MutableLiveData(state)
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<AdmissionScenariosFragment>(
            testNavHostController = navController
        )
        takeScreenshot<AdmissionScenariosFragment>()
    }
}
