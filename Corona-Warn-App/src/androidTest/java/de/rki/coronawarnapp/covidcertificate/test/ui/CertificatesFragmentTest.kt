package de.rki.coronawarnapp.covidcertificate.test.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.ui.cards.CovidTestCertificateCard
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
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchInMainActivity
import testhelpers.selectBottomNavTab
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
@Ignore("To be removed")
class CertificatesFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: CertificatesViewModel
    @MockK lateinit var vaccinatedPerson: VaccinatedPerson

    private val formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm")
    private val testDate = DateTime.parse("12.05.2021 19:00", formatter).toInstant()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { vaccinatedPerson.fullName } returns "Andrea Schneider"
        every { vaccinatedPerson.getMostRecentVaccinationCertificate.expiresAt } returns
            testDate.plus(Duration.standardDays(365)).toInstant()

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
                    testDate = testDate,
                    testPerson = "Andrea Schneider"
                ) { }
            )
        )
    }

    @Screenshot
    @Test
    fun capture_screenshot_pending_certificate() {
        every { vaccinatedPerson.getVaccinationStatus() } returns VaccinatedPerson.Status.IMMUNITY
        takeScreenshotInMainActivity("pending")
    }

    private fun takeScreenshotInMainActivity(suffix: String = "") {
        launchInMainActivity<CertificatesFragment>()
        onView(withId(R.id.fake_bottom_navigation)).perform(selectBottomNavTab(R.id.certificate_graph))
        takeScreenshot<CertificatesFragment>(suffix)
    }
}

@Module
abstract class CertificatesFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun certificatesFragment(): CertificatesFragment
}
