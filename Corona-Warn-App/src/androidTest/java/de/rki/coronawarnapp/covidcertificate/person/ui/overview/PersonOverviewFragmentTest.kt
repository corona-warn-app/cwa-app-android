package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
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
import testhelpers.launchFragment2
import testhelpers.launchInMainActivity
import testhelpers.selectBottomNavTab
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class PersonOverviewFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: PersonOverviewViewModel

    private lateinit var bitmap: Bitmap

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { personCertificates } returns MutableLiveData()
            every { markNewCertsAsSeen } returns MutableLiveData()
        }

        bitmap = BitmapFactory.decodeResource(
            ApplicationProvider.getApplicationContext<Context>().resources,
            R.drawable.test_qr_code
        )

        setupMockViewModel(
            object : PersonOverviewViewModel.Factory {
                override fun create(): PersonOverviewViewModel = viewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<PersonOverviewFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment_empty() = takeSelfie("empty")

    @Test
    @Screenshot
    fun capture_fragment_pending() {
        every { viewModel.personCertificates } returns MutableLiveData(listItemWithPendingItem())
        takeSelfie("pending")
    }

    @Test
    @Screenshot
    fun capture_fragment_updating() {
        every { viewModel.personCertificates } returns MutableLiveData(listItemWithUpdatingItem())
        takeSelfie("updating")
    }

    @Test
    @Screenshot
    fun capture_fragment_persons() {
        every { viewModel.personCertificates } returns MutableLiveData(personsItems())
        takeSelfie("persons")
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    private fun takeSelfie(suffix: String) {
        launchInMainActivity<PersonOverviewFragment>()
        onView(withId(R.id.fake_bottom_navigation)).perform(selectBottomNavTab(R.id.covid_certificates_graph))
        takeScreenshot<PersonOverviewFragment>(suffix)
    }

    private fun listItemWithPendingItem() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                CovidTestCertificatePendingCard.Item(
                    certificate = mockTestCertificate("Andrea Schneider", isPending = true),
                    onDeleteAction = {},
                    onRetryAction = {},
                )
            )

            add(
                PersonCertificateCard.Item(
                    certificate = mockTestCertificate("Andrea Schneider"),
                    onClickAction = { _, _ -> },
                    colorShade = PersonColorShade.COLOR_1,
                    qrcodeBitmap = bitmap
                )
            )
        }

    private fun listItemWithUpdatingItem() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                CovidTestCertificatePendingCard.Item(
                    certificate = mockTestCertificate("Andrea Schneider", isPending = true, isUpdating = true),
                    onDeleteAction = {},
                    onRetryAction = {},
                )
            )

            add(
                PersonCertificateCard.Item(
                    certificate = mockTestCertificate("Andrea Schneider"),
                    onClickAction = { _, _ -> },
                    colorShade = PersonColorShade.COLOR_1,
                    qrcodeBitmap = bitmap
                )
            )
        }

    private fun personsItems() = mutableListOf<PersonCertificatesItem>()
        .apply {
            add(
                PersonCertificateCard.Item(
                    certificate = mockTestCertificate("Andrea Schneider"),
                    onClickAction = { _, _ -> },
                    colorShade = PersonColorShade.COLOR_1,
                    qrcodeBitmap = bitmap
                )
            )

            add(
                PersonCertificateCard.Item(
                    certificate = mockTestCertificate("Mia Schneider"),
                    onClickAction = { _, _ -> },
                    colorShade = PersonColorShade.COLOR_2,
                    qrcodeBitmap = bitmap
                )
            )

            add(
                PersonCertificateCard.Item(
                    certificate = mockTestCertificate("Thomas Schneider"),
                    onClickAction = { _, _ -> },
                    colorShade = PersonColorShade.COLOR_3,
                    qrcodeBitmap = bitmap
                )
            )
        }

    private fun mockTestCertificate(
        name: String,
        isPending: Boolean = false,
        isUpdating: Boolean = false
    ): TestCertificate = mockk<TestCertificate>().apply {
        every { isCertificateRetrievalPending } returns isPending
        every { isUpdatingData } returns isUpdating
        every { fullName } returns name
        every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirthFormatted = "1943-04-18"
        )
    }
}

@Module
abstract class PersonOverviewFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun personOverviewFragment(): PersonOverviewFragment
}
