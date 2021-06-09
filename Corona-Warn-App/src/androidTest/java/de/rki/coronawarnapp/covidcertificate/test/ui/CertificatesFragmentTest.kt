package de.rki.coronawarnapp.covidcertificate.test.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.cards.CovidTestCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.ui.cards.CovidTestCertificateErrorCard
import de.rki.coronawarnapp.covidcertificate.test.ui.items.CertificatesItem
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.CreateVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.HeaderInfoVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.ImmuneVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.NoCovidTestCertificatesCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.VaccinationCard
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchInMainActivity
import testhelpers.selectBottomNavTab
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class CertificatesFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: CertificatesViewModel
    @MockK lateinit var vaccinatedPerson: VaccinatedPerson

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { vaccinatedPerson.fullName } returns "Max Mustermann"
        every { vaccinatedPerson.getMostRecentVaccinationCertificate.expiresAt } returns
            DateTime.now().plusDays(365).toInstant()

        setupMockViewModel(
            object : CertificatesViewModel.Factory {
                override fun create(): CertificatesViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<CertificatesFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot_empty() {
        every { viewModel.screenItems } returns getEmptyScreenItems()

        takeScreenshotInMainActivity()
    }

    private fun getEmptyScreenItems(): LiveData<List<CertificatesItem>> {
        return MutableLiveData(
            listOf(
                HeaderInfoVaccinationCard.Item,
                CreateVaccinationCard.Item {},
                NoCovidTestCertificatesCard.Item
            )
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot_vaccination_incomplete() {
        every { vaccinatedPerson.getVaccinationStatus() } returns VaccinatedPerson.Status.INCOMPLETE
        every { viewModel.screenItems } returns getVaccinationIncompleteScreenItems()

        takeScreenshotInMainActivity("incomplete")
    }

    private fun getVaccinationIncompleteScreenItems(): LiveData<List<CertificatesItem>> {
        return MutableLiveData(
            listOf(
                HeaderInfoVaccinationCard.Item,
                VaccinationCard.Item(
                    vaccinatedPerson = vaccinatedPerson,
                    onClickAction = {}
                ),
                NoCovidTestCertificatesCard.Item
            )
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot_vaccination_complete() {
        every { vaccinatedPerson.getVaccinationStatus() } returns VaccinatedPerson.Status.IMMUNITY
        every { viewModel.screenItems } returns getVaccinationImmuneScreenItems()

        takeScreenshotInMainActivity("immune")
    }

    private fun getVaccinationImmuneScreenItems(): LiveData<List<CertificatesItem>> {
        return MutableLiveData(
            listOf(
                HeaderInfoVaccinationCard.Item,
                ImmuneVaccinationCard.Item(
                    vaccinatedPerson = vaccinatedPerson,
                    onClickAction = {}
                ),
                NoCovidTestCertificatesCard.Item
            )
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot_green_certificate() {
        every { vaccinatedPerson.getVaccinationStatus() } returns VaccinatedPerson.Status.IMMUNITY
        every { viewModel.screenItems } returns getVaccinationGreenCertScreenItems()

        takeScreenshotInMainActivity("green")
    }

    private fun getVaccinationGreenCertScreenItems(): LiveData<List<CertificatesItem>> {
        return MutableLiveData(
            listOf(
                HeaderInfoVaccinationCard.Item,
                ImmuneVaccinationCard.Item(
                    vaccinatedPerson = vaccinatedPerson,
                    onClickAction = {}
                ),
                CovidTestCertificateCard.Item(
                    testDate = DateTime.now().toInstant(),
                    testPerson = "Max Mustermann"
                ) { }
            )
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot_pending_certificate() {
        every { vaccinatedPerson.getVaccinationStatus() } returns VaccinatedPerson.Status.IMMUNITY
        every { viewModel.screenItems } returns getVaccinationPendingCertScreenItems()

        takeScreenshotInMainActivity("pending")
    }

    private fun getVaccinationPendingCertScreenItems(): LiveData<List<CertificatesItem>> {
        return MutableLiveData(
            listOf(
                HeaderInfoVaccinationCard.Item,
                ImmuneVaccinationCard.Item(
                    vaccinatedPerson = vaccinatedPerson,
                    onClickAction = {}
                ),
                CovidTestCertificateErrorCard.Item(
                    testDate = DateTime.now().toInstant(),
                ) { }
            )
        )
    }

    private fun takeScreenshotInMainActivity(suffix: String = "") {
        launchInMainActivity<CertificatesFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.fake_bottom_navigation))
            .perform(selectBottomNavTab(R.id.green_certificate_graph))
        takeScreenshot<CertificatesFragment>(suffix)
    }
}

@Module
abstract class CertificatesFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun certificatesFragment(): CertificatesFragment
}
