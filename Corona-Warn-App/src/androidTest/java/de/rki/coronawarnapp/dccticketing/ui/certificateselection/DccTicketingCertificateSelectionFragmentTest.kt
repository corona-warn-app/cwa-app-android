package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingNoValidCertificateCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingNoValidCertificateFaqCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingNoValidCertificateHeaderCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingVaccinationCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingValidCertificateHeaderCard
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class DccTicketingCertificateSelectionFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: DccTicketingCertificateSelectionViewModel
    private val vc = DccTicketingValidationCondition(
        lang = "en-en",
        fnt = "SCHNEIDER",
        gnt = "ANDREA",
        dob = "1989-12-12",
        coa = "AF",
        cod = "SJ",
        roa = "AF",
        rod = "SJ",
        type = listOf(
            "r",
            "v",
            "t"
        ),
        category = listOf(
            "Standard"
        ),
        validationClock = "2021-11-03T15:39:43+00:00",
        validFrom = "2021-11-03T07:15:43+00:00",
        validTo = "2021-11-03T15:39:43+00:00",
        hash = null
    )

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.dccTicketingCertificateSelectionFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : DccTicketingCertificateSelectionViewModel.Factory {
                override fun create(
                    dccTicketingSharedViewModel: DccTicketingSharedViewModel
                ): DccTicketingCertificateSelectionViewModel = viewModel
            }
        )
    }

    @Screenshot
    @Test
    fun dccTicketingCertificateSelectionFragment_no_certificates() {
        every { viewModel.items } returns MutableLiveData(
            listOf(
                DccTicketingNoValidCertificateHeaderCard.Item(),
                DccTicketingNoValidCertificateCard.Item(vc),
                DccTicketingNoValidCertificateFaqCard.Item()
            )

        )
        launchFragmentInContainer2<DccTicketingCertificateSelectionFragment>(
            testNavHostController = navController
        )
        takeScreenshot<DccTicketingCertificateSelectionFragment>("no_certificates")
    }

    @Screenshot
    @Test
    fun dccTicketingCertificateSelectionFragment_certificates() {
        every { viewModel.items } returns MutableLiveData(
            listOf(
                DccTicketingValidCertificateHeaderCard.Item(vc),
                DccTicketingVaccinationCard.Item(
                    mockk<VaccinationCertificate>().apply {
                        every { totalSeriesOfDoses } returns 2
                        every { doseNumber } returns 2
                        every { vaccinatedOn } returns LocalDate.now()
                        every { containerId } returns VaccinationCertificateContainerId("id")
                    }
                ) {}
            )

        )
        launchFragmentInContainer2<DccTicketingCertificateSelectionFragment>(
            testNavHostController = navController
        )
        takeScreenshot<DccTicketingCertificateSelectionFragment>("certificates")
    }
}

@Module
abstract class DccTicketingCertificateSelectionFragmentModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingCertificateSelectionFragment(): DccTicketingCertificateSelectionFragment
}
