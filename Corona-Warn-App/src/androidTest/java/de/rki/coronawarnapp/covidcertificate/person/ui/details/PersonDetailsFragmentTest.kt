package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.PersonDetailsQrCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class PersonDetailsFragmentTest : BaseUITest() {
    @MockK lateinit var viewModel: PersonDetailsViewModel
    private lateinit var bitmap: Bitmap
    private val args = PersonDetailsFragmentArgs("code").toBundle()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { uiState } returns MutableLiveData()
        }

        bitmap = BitmapFactory.decodeResource(
            ApplicationProvider.getApplicationContext<Context>().resources,
            R.drawable.test_qr_code
        )
        setupMockViewModel(
            object : PersonDetailsViewModel.Factory {
                override fun create(personIdentifierCode: String): PersonDetailsViewModel = viewModel
            }
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<PersonDetailsFragment>(fragmentArgs = args)
    }

    @Test
    @Screenshot
    fun capture_fragment_cwa_user() {
        every { viewModel.uiState } returns certificateData(true)

        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("cwa")

        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<PersonDetailsFragment>("cwa_2")
    }

    @Test
    @Screenshot
    fun capture_fragment_not_cwa_user() {
        every { viewModel.uiState } returns certificateData()
        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("not_cwa")
    }

    private fun certificateData(isCwa: Boolean = false): LiveData<List<CertificateItem>> = MutableLiveData(
        mutableListOf<CertificateItem>().apply {
            val testCertificate = mockTestCertificate()
            val vaccinationCertificate1 = mockVaccinationCertificate(number = 1, final = false)
            val vaccinationCertificate2 = mockVaccinationCertificate(number = 2, final = true)
            val recoveryCertificate = mockRecoveryCertificate()
            val personCertificates = PersonCertificates(
                listOf(testCertificate, vaccinationCertificate1, vaccinationCertificate2), isCwaUser = isCwa
            )

            add(PersonDetailsQrCard.Item(testCertificate, bitmap))
            add(CwaUserCard.Item(personCertificates) {})
            add(VaccinationCertificateCard.Item(vaccinationCertificate1, isCurrentCertificate = false) {})
            add(VaccinationCertificateCard.Item(vaccinationCertificate2, isCurrentCertificate = false) {})
            add(TestCertificateCard.Item(testCertificate, isCurrentCertificate = true) {})
            add(RecoveryCertificateCard.Item(recoveryCertificate, isCurrentCertificate = false) {})
        }
    )

    private fun mockTestCertificate(): TestCertificate = mockk<TestCertificate>().apply {
        every { certificateId } returns "testCertificateId"
        every { fullName } returns "Andrea Schneider"
        every { testType } returns "PCR-Test"
        every { dateOfBirth } returns LocalDate.parse("18.04.1943", DateTimeFormat.forPattern("dd.MM.yyyy"))
        every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
        every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirth = LocalDate.now()
        )
    }

    private fun mockVaccinationCertificate(
        number: Int = 1,
        final: Boolean = false
    ): VaccinationCertificate =
        mockk<VaccinationCertificate>().apply {
            every { certificateId } returns "vaccinationCertificateId$number"
            every { vaccinatedAt } returns Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
            every { personIdentifier } returns CertificatePersonIdentifier(
                firstNameStandardized = "firstNameStandardized",
                lastNameStandardized = "lastNameStandardized",
                dateOfBirth = LocalDate.now()
            )
            every { doseNumber } returns number
            every { totalSeriesOfDoses } returns 2
            every { isFinalShot } returns final
        }

    private fun mockRecoveryCertificate(): RecoveryCertificate =
        mockk<RecoveryCertificate>().apply {
            every { certificateId } returns "recoveryCertificateId"
            every { validUntil } returns Instant.parse("2021-05-31T11:35:00.000Z").toLocalDateUserTz()
        }

    @After
    fun tearDown() {
        clearAllViewModels()
    }
}

@Module
abstract class PersonDetailsFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun personDetailsFragment(): PersonDetailsFragment
}
