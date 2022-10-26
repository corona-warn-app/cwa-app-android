package de.rki.coronawarnapp.dccreissuance.ui.consent

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.liveData
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.betterScrollTo
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DccReissuanceConsentFragmentTest : BaseUITest() {

    @RelaxedMockK lateinit var viewModel: DccReissuanceConsentViewModel

    private val navController = TestNavHostController(
        context = ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.covid_certificates_graph)
            setCurrentDestination(R.id.dccReissuanceConsentFragment)
        }
    }

    private val args = DccReissuanceConsentFragmentArgs(groupKey = "personIdentifierCode").toBundle()

    private val state = DccReissuanceConsentViewModel.State(
        certificateList = mutableListOf<DccReissuanceItem>(
            DccReissuanceCertificateCard.Item(
                mockk<RecoveryDccV1> {
                    every { nameData } returns mockk {
                        every { fullName } returns "Andrea Schneider"
                    }

                    every { recovery } returns mockk {
                        every { testedPositiveOn } returns LocalDate.parse("2022-01-15")
                        every { personIdentifier } returns CertificatePersonIdentifier(
                            dateOfBirthFormatted = "2020-01-01",
                            lastNameStandardized = "Schneider",
                            firstNameStandardized = "Andrea"
                        )
                    }
                }
            ),
            DccReissuanceCertificateCard.Item(
                mockk<VaccinationDccV1> {
                    every { nameData } returns mockk {
                        every { fullName } returns "Andrea Schneider"
                    }

                    every { vaccination } returns mockk {
                        every { doseNumber } returns 2
                        every { totalSeriesOfDoses } returns 2
                        every { isSeriesCompletingShot } returns true
                        every { vaccinatedOn } returns LocalDate.parse("2022-01-17")
                        every { personIdentifier } returns CertificatePersonIdentifier(
                            dateOfBirthFormatted = "2020-01-01",
                            lastNameStandardized = "Schneider",
                            firstNameStandardized = "Andrea"
                        )
                    }
                }
            )
        ),
        divisionVisible = true,
        listItemsTitle = "Zu erneuernde Zertifikate:",
        title = "Zertifikat aktualisieren",
        subtitle = "Neuausstellung direkt über die App vornehmen.",
        content = "Die Spezifikationen der EU für Zertifikate von Auffrischimpfungen wurden geändert. Dieses Zertifikat entspricht nicht den aktuellen Spezifikationen. Das Impfzertifikat ist zwar weiterhin gültig, es kann jedoch sein, dass bei einer Prüfung die Auffrischimpfung nicht erkannt wird. Bitte lassen Sie sich daher ein neues Impfzertifikat ausstellen.\n\nSie können ein neues Impfzertifikat direkt kostenlos über die App anfordern. Hierfür ist Ihr Einverständnis erforderlich.",
        url = null,
        accompanyingCertificatesVisible = true
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { viewModel.stateLiveData } returns liveData { emit(state) }

        setupMockViewModel(
            factory = object : DccReissuanceConsentViewModel.Factory {
                override fun create(groupKey: String): DccReissuanceConsentViewModel = viewModel
            }
        )
    }

    @After
    fun tearDown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<DccReissuanceConsentFragment>(fragmentArgs = args)
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<DccReissuanceConsentFragment>(
            testNavHostController = navController,
            fragmentArgs = args
        )
        takeScreenshot<DccReissuanceConsentFragment>("1")

        // Take hint and consent part screenshot
        Espresso.onView(ViewMatchers.withId(R.id.dcc_reissuance_consent)).perform(betterScrollTo())
        takeScreenshot<DccReissuanceConsentFragment>("2")

        // Take more info part screenshot
        Espresso.onView(ViewMatchers.withId(R.id.privacy_information)).perform(betterScrollTo())
        takeScreenshot<DccReissuanceConsentFragment>("3")
    }
}

@Module
abstract class DccReissuanceConsentFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun dccReissuanceConsentFragment(): DccReissuanceConsentFragment
}
