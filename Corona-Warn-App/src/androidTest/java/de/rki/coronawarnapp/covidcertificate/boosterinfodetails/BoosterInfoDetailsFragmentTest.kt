package de.rki.coronawarnapp.covidcertificate.boosterinfodetails

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.createFakeImageLoaderForQrCodes
import testhelpers.launchFragmentInContainer2
import testhelpers.setupFakeImageLoader
import testhelpers.takeScreenshot

@HiltAndroidTest
class BoosterInfoDetailsFragmentTest : BaseUITest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    val viewModel = mockk<BoosterInfoDetailsViewModel>(relaxed = true)

    @BindValue
    @JvmField
    val factory: BoosterInfoDetailsViewModel.FactoryI = object : BoosterInfoDetailsViewModel.FactoryI {
        override fun create(groupKey: String): BoosterInfoDetailsViewModel = viewModel
    }

    private val args = BoosterInfoDetailsFragmentArgs("code").toBundle()

    @Before
    fun setUp() {
        setupFakeImageLoader(createFakeImageLoaderForQrCodes())
    }

    @Test
    fun launch_fragment() {
        launchFragmentInContainer2<BoosterInfoDetailsFragment>(fragmentArgs = args)
    }

    @Test
    @Screenshot
    fun capture() {
        every { viewModel.uiState } returns MutableLiveData(
            BoosterInfoDetailsViewModel.UiState(
                titleText = "Booster",
                subtitleText = "Empfehlung einer Booster-Impfung",
                longText = "Die Ständige Impfkommission (STIKO) empfiehlt allen Personen eine weitere Impfstoffdosis " +
                    "zur Optimierung der Grundimmunisierung, die mit einer Dosis des Janssen-Impfstoffs (Johnson & Johnson) " +
                    "grundimmunisiert wurden, bei denen keine Infektion mit dem Coronavirus SARS-CoV-2 nachgewiesen wurde und " +
                    "wenn ihre Janssen-Impfung über 4 Wochen her ist.\n\n Da Sie laut Ihrer gespeicherten Zertifikate bald " +
                    "dieser Personengruppe angehören und noch keine weitere Impfung erhalten haben, möchten wir Sie auf diese " +
                    "Empfehlung hinweisen. (Regel BNR-DE-0200)\n\n Dieser Hinweis basiert ausschließlich auf den auf Ihrem " +
                    "Smartphone gespeicherten Zertifikaten. Die Verarbeitung der Daten erfolgte auf Ihrem Smartphone. Es wurden " +
                    "hierbei keine Daten an das RKI oder Dritte übermittelt.\n" +
                    "Mehr Informationen finden Sie in den FAQ.",
                faqUrl = "dcc_admission_state"
            )
        )

        launchFragmentInContainer2<BoosterInfoDetailsFragment>(fragmentArgs = args)
        takeScreenshot<BoosterInfoDetailsFragment>("1")

        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<BoosterInfoDetailsFragment>("2")
    }
}
