package de.rki.coronawarnapp.vaccination.ui.list

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.IMMUNITY
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListImmunityInformationCardItemVH.VaccinationListImmunityInformationCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListQrCodeCardItemVH.VaccinationListQrCodeCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
internal class VaccinationListFragmentTest : BaseUITest() {

    @MockK lateinit var vaccinationListViewModel: VaccinationListViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private val applicationContext = ApplicationProvider.getApplicationContext<Context>()
    private val testQrCode = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test_qr_code)
    private val formatter = DateTimeFormat.forPattern("dd.MM.yyyy")

    private val fragmentArgs = VaccinationListFragmentArgs("personIdentifierCodeSha256").toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : VaccinationListViewModel.Factory {
                override fun create(personIdentifierCode: String): VaccinationListViewModel = vaccinationListViewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<VaccinationListFragment>(fragmentArgs = fragmentArgs)
    }

    @Screenshot
    @Test
    fun capture_screenshots_incomplete() {

        val listItems = listOf(
            createQrCodeCardItem(
                doseNumber = 1,
                totalSeriesOfDoses = 2
            ),
            createNameCardItem(),
            createVaccinationCardItem(
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                vaccinationStatus = INCOMPLETE
            )
        )

        every { vaccinationListViewModel.uiState } returns mockUiState(
            itemList = listItems,
            vaccinationStatus = INCOMPLETE
        )
        launchFragmentInContainer2<VaccinationListFragment>(fragmentArgs = fragmentArgs)
        takeScreenshot<VaccinationListFragment>("incomplete")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationListFragment>("incomplete_scrolled_down")
    }

    @Screenshot
    @Test
    fun capture_screenshots_complete() {

        val listItems = listOf(
            createQrCodeCardItem(
                doseNumber = 2,
                totalSeriesOfDoses = 2,
                vaccinatedAt = LocalDate.parse("24.04.2021", formatter),
                expiresAt = Instant.parse("2022-04-24T00:00:00.000Z")
            ),
            createNameCardItem(),
            VaccinationListImmunityInformationCardItem(Duration.standardDays(14)),
            createVaccinationCardItem(
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                vaccinationStatus = COMPLETE
            ),
            createVaccinationCardItem(
                doseNumber = 2,
                totalSeriesOfDoses = 2,
                vaccinationStatus = COMPLETE,
                vaccinatedAt = LocalDate.parse("24.04.2021", formatter)
            )
        )

        every { vaccinationListViewModel.uiState } returns mockUiState(
            itemList = listItems,
            vaccinationStatus = COMPLETE
        )
        launchFragmentInContainer2<VaccinationListFragment>(fragmentArgs = fragmentArgs)
        takeScreenshot<VaccinationListFragment>("complete")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationListFragment>("complete_scrolled_down")
    }

    @Screenshot
    @Test
    fun capture_screenshots_immunity() {
        val listItems = listOf(
            createQrCodeCardItem(
                doseNumber = 2,
                totalSeriesOfDoses = 2,
                vaccinatedAt = LocalDate.parse("24.04.2021", formatter),
                expiresAt = Instant.parse("2022-04-24T00:00:00.000Z")
            ),
            createNameCardItem(),
            createVaccinationCardItem(
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                vaccinationStatus = IMMUNITY
            ),
            createVaccinationCardItem(
                doseNumber = 2,
                totalSeriesOfDoses = 2,
                vaccinationStatus = IMMUNITY,
                vaccinatedAt = LocalDate.parse("24.04.2021", formatter)
            )
        )

        every { vaccinationListViewModel.uiState } returns mockUiState(
            itemList = listItems,
            vaccinationStatus = IMMUNITY
        )
        launchFragmentInContainer2<VaccinationListFragment>(fragmentArgs = fragmentArgs)
        takeScreenshot<VaccinationListFragment>("immunity")
        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<VaccinationListFragment>("immunity_scrolled_down")
    }

    private fun mockUiState(itemList: List<VaccinationListItem>, vaccinationStatus: VaccinatedPerson.Status) =
        MutableLiveData(
            VaccinationListViewModel.UiState(
                listItems = itemList,
                vaccinationStatus = vaccinationStatus
            )
        )

    private fun createVaccinationCardItem(
        doseNumber: Int,
        totalSeriesOfDoses: Int,
        vaccinationStatus: VaccinatedPerson.Status,
        vaccinatedAt: LocalDate = LocalDate.parse("12.04.2021", formatter)
    ) = VaccinationListVaccinationCardItem(
        vaccinationCertificateId = "vaccinationCertificateId",
        doseNumber = doseNumber,
        totalSeriesOfDoses = totalSeriesOfDoses,
        vaccinatedAt = vaccinatedAt.toDayFormat(),
        vaccinationStatus = vaccinationStatus,
        isFinalVaccination = doseNumber == totalSeriesOfDoses,
        onCardClick = {},
        onDeleteClick = {},
        onSwipeToDelete = { _, _ -> }
    )

    private fun createNameCardItem() = VaccinationListNameCardItem(
        fullName = "Max Mustermann",
        dayOfBirth = LocalDate.parse("01.02.1976", formatter).toDayFormat()
    )

    private fun createQrCodeCardItem(
        doseNumber: Int,
        totalSeriesOfDoses: Int,
        vaccinatedAt: LocalDate = LocalDate.parse("12.04.2021", formatter),
        expiresAt: Instant = Instant.parse("2022-04-12T00:00:00.000Z")
    ) = VaccinationListQrCodeCardItem(
        qrCode = testQrCode,
        doseNumber = doseNumber,
        totalSeriesOfDoses = totalSeriesOfDoses,
        vaccinatedAt = vaccinatedAt,
        expiresAt = expiresAt
    ) {}

    @After
    fun tearDown() {
        clearAllViewModels()
    }
}

@Module
abstract class VaccinationListFragmentTestModule {

    @ContributesAndroidInjector
    abstract fun vaccinationListFragment(): VaccinationListFragment
}
