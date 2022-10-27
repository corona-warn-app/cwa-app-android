package de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.liveData
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceCertificateCard
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceItem
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
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DccReissuanceAccCertsFragmentTest : BaseUITest() {

    @RelaxedMockK lateinit var viewModel: DccReissuanceAccCertsViewModel

    private val navController = TestNavHostController(
        context = ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.covid_certificates_graph)
            setCurrentDestination(R.id.dccReissuanceAccCertsFragment)
        }
    }

    private val args = DccReissuanceAccCertsFragmentArgs(groupKey = "personIdentifierCode").toBundle()

    private val certificateList = mutableListOf<DccReissuanceItem>(
        DccReissuanceCertificateCard.Item(
            mockk<VaccinationDccV1> {
                every { nameData } returns mockk {
                    every { fullName } returns "Andrea Schneider"
                }

                every { vaccination } returns mockk {
                    every { doseNumber } returns 2
                    every { totalSeriesOfDoses } returns 2
                    every { isSeriesCompletingShot } returns true
                    every { vaccinatedOn } returns LocalDate.parse("2022-01-15")
                    every { personIdentifier } returns CertificatePersonIdentifier(
                        dateOfBirthFormatted = "1980-06-01",
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
                    every { doseNumber } returns 1
                    every { totalSeriesOfDoses } returns 2
                    every { isSeriesCompletingShot } returns false
                    every { vaccinatedOn } returns LocalDate.parse("2021-10-17")
                    every { personIdentifier } returns CertificatePersonIdentifier(
                        dateOfBirthFormatted = "1980-06-01",
                        lastNameStandardized = "Schneider",
                        firstNameStandardized = "Andrea"
                    )
                }
            }
        )
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { viewModel.certificatesLiveData } returns liveData { emit(certificateList) }

        setupMockViewModel(
            factory = object : DccReissuanceAccCertsViewModel.Factory {
                override fun create(personIdentifierCode: String): DccReissuanceAccCertsViewModel = viewModel
            }
        )
    }

    @After
    fun tearDown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<DccReissuanceAccCertsFragment>(fragmentArgs = args)
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<DccReissuanceAccCertsFragment>(
            testNavHostController = navController,
            fragmentArgs = args
        )
        takeScreenshot<DccReissuanceAccCertsFragment>("1")
    }
}

@Module
abstract class DccReissuanceAccCertsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun dccReissuanceAccCertsFragment(): DccReissuanceAccCertsFragment
}
