package de.rki.coronawarnapp.covidcertificate.person.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.PersonDetailsQrCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationInfoCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
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
import testhelpers.createFakeImageLoaderForQrCodes
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.setupFakeImageLoader
import testhelpers.stringForLocale
import testhelpers.takeScreenshot
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class PersonDetailsFragmentTest : BaseUITest() {
    @MockK lateinit var viewModel: PersonDetailsViewModel
    private val args = PersonDetailsFragmentArgs("code").toBundle()
    private val vcContainerId = VaccinationCertificateContainerId("1")
    private val tcsContainerId = TestCertificateContainerId("2")
    private val rcContainerId = RecoveryCertificateContainerId("3")

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel.apply {
            every { events } returns SingleLiveEvent()
            every { uiState } returns MutableLiveData()
        }
        setupFakeImageLoader(
            createFakeImageLoaderForQrCodes()
        )
        setupMockViewModel(
            object : PersonDetailsViewModel.Factory {
                override fun create(
                    personIdentifierCode: String,
                    colorShade: PersonColorShade,
                    containerId: CertificateContainerId?,
                    savedInstance: SavedStateHandle
                ): PersonDetailsViewModel = viewModel
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

    @Test
    @Screenshot
    fun capture_fragment_booster() {
        every { viewModel.uiState } returns boosterCertificateData()
        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("booster")

        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<PersonDetailsFragment>("booster_2")
    }

    private fun certificateData(isCwa: Boolean = false): LiveData<List<CertificateItem>> = MutableLiveData(
        mutableListOf<CertificateItem>().apply {
            val testCertificate = mockTestCertificate()
            val vaccinationCertificate1 = mockVaccinationCertificate(number = 1, final = false)
            val vaccinationCertificate2 = mockVaccinationCertificate(number = 2, final = true)
            val vaccinationCertificate3 = mockVaccinationCertificate(number = 3, final = false, booster = true)
            val recoveryCertificate = mockRecoveryCertificate()
            val personCertificates = PersonCertificates(
                listOf(testCertificate, vaccinationCertificate1, vaccinationCertificate2, vaccinationCertificate3),
                isCwaUser = isCwa
            )

            add(PersonDetailsQrCard.Item(testCertificate, false) {})

            add(
                VaccinationInfoCard.Item(
                    vaccinationStatus = VaccinatedPerson.Status.IMMUNITY,
                    daysUntilImmunity = null,
                    boosterRule = null,
                    daysSinceLastVaccination = 86,
                    hasBoosterNotification = false
                )
            )

            add(CwaUserCard.Item(personCertificates) {})
            add(
                VaccinationCertificateCard.Item(
                    vaccinationCertificate1,
                    isCurrentCertificate = false,
                    status = VaccinatedPerson.Status.COMPLETE,
                    colorShade = PersonColorShade.COLOR_1
                ) {}
            )
            add(
                VaccinationCertificateCard.Item(
                    vaccinationCertificate2,
                    isCurrentCertificate = false,
                    status = VaccinatedPerson.Status.COMPLETE,
                    colorShade = PersonColorShade.COLOR_1
                ) {}
            )

            add(
                VaccinationCertificateCard.Item(
                    vaccinationCertificate3,
                    isCurrentCertificate = false,
                    status = VaccinatedPerson.Status.IMMUNITY,
                    colorShade = PersonColorShade.COLOR_1
                ) {}
            )

            add(TestCertificateCard.Item(testCertificate, isCurrentCertificate = true, PersonColorShade.COLOR_1) {})
            add(
                RecoveryCertificateCard.Item(
                    recoveryCertificate,
                    isCurrentCertificate = false,
                    PersonColorShade.COLOR_1
                ) {}
            )
        }
    )

    private fun boosterCertificateData(isCwa: Boolean = false): LiveData<List<CertificateItem>> = MutableLiveData(
        mutableListOf<CertificateItem>().apply {
            val vaccinationCertificate1 = mockVaccinationCertificate(number = 3, final = false, booster = true)

            val personCertificates = PersonCertificates(
                listOf(vaccinationCertificate1),
                isCwaUser = isCwa
            )


            val ruleDescriptionDE = mockk<DccValidationRule.Description> {
                Locale.GERMAN.also {
                    every { description } returns "Sie könnten für eine Auffrischungsimpfung berechtigt sein, da sie for mehr als 4 Monaten von COVID-19 genesen sind trotz einer vorherigen Impfung."
                    every { languageCode } returns it.language
                }
            }

            val ruleDescriptionEN = mockk<DccValidationRule.Description> {
                Locale.ENGLISH.also {
                    every { description } returns "You may be eligible for a booster because you recovered from COVID-19 more than 4 months ago despite a prior vaccination."
                    every { languageCode } returns it.language
                }
            }

            add(PersonDetailsQrCard.Item(vaccinationCertificate1, false) {})

            add(
                VaccinationInfoCard.Item(
                    vaccinationStatus = VaccinatedPerson.Status.BOOSTER_ELIGIBLE,
                    daysUntilImmunity = null,
                    boosterRule = mockk<DccValidationRule>().apply {
                        every { identifier } returns "BNR-DE-4161"
                        every { description } returns listOf(ruleDescriptionDE, ruleDescriptionEN)
                    },
                    daysSinceLastVaccination = 147,
                    hasBoosterNotification = true
                )
            )

            add(CwaUserCard.Item(personCertificates) {})
            add(
                VaccinationCertificateCard.Item(
                    vaccinationCertificate1,
                    isCurrentCertificate = true,
                    status = VaccinatedPerson.Status.COMPLETE,
                    colorShade = PersonColorShade.COLOR_1
                ) {}
            )
        }
    )

    private fun mockTestCertificate(): TestCertificate = mockk<TestCertificate>().apply {
        every { certificateId } returns "testCertificateId"
        every { fullName } returns "Andrea Schneider"
        every { rawCertificate } returns mockk<TestDccV1>().apply {
            every { test } returns mockk<DccV1.TestCertificateData>().apply {
                every { testType } returns "LP6464-4"
                every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
            }
        }
        every { headerExpiresAt } returns Instant.now().plus(20)
        every { containerId } returns tcsContainerId
        every { testType } returns "PCR-Test"
        every { dateOfBirthFormatted } returns "1943-04-18"
        every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
        every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { personIdentifier } returns certificatePersonIdentifier
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.testCertificate)
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirthFormatted = "1943-04-18"
        )
        every { isValid } returns true
        every { sampleCollectedAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { getState() } returns CwaCovidCertificate.State.Valid(headerExpiresAt)
    }

    private fun mockVaccinationCertificate(
        number: Int = 1,
        final: Boolean = false,
        booster: Boolean = false
    ): VaccinationCertificate =
        mockk<VaccinationCertificate>().apply {
            val localDate = Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
            every { fullName } returns "Andrea Schneider"
            every { certificateId } returns "vaccinationCertificateId$number"
            every { rawCertificate } returns mockk<VaccinationDccV1>().apply {
                every { vaccination } returns mockk<DccV1.VaccinationData>().apply {
                    every { doseNumber } returns number
                    every { totalSeriesOfDoses } returns 2
                    every { vaccinatedOn } returns localDate
                }
            }
            every { containerId } returns vcContainerId
            every { vaccinatedOn } returns localDate
            every { personIdentifier } returns certificatePersonIdentifier
            every { vaccinatedOn } returns Instant.parse("2021-04-01T11:35:00.000Z").toLocalDateUserTz()
            every { personIdentifier } returns CertificatePersonIdentifier(
                firstNameStandardized = "firstNameStandardized",
                lastNameStandardized = "lastNameStandardized",
                dateOfBirthFormatted = "1943-04-18"
            )
            every { doseNumber } returns number
            every { totalSeriesOfDoses } returns 2
            every { dateOfBirthFormatted } returns "1981-03-20"
            every { isFinalShot } returns final
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
            every { isValid } returns true
            every { getState() } returns CwaCovidCertificate.State.Valid(Instant.now().plus(20))
        }

    private fun mockRecoveryCertificate(): RecoveryCertificate =
        mockk<RecoveryCertificate>().apply {
            every { fullName } returns "Andrea Schneider"
            every { certificateId } returns "recoveryCertificateId"
            every { dateOfBirthFormatted } returns "1981-03-20"
            every { validUntil } returns Instant.parse("2021-03-31T11:35:00.000Z").toLocalDateUserTz()
            every { personIdentifier } returns certificatePersonIdentifier
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.recoveryCertificate)
            every { containerId } returns rcContainerId
            every { isValid } returns true
            every { getState() } returns CwaCovidCertificate.State.Valid(Instant.now().plus(20))
        }

    private val certificatePersonIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1981-03-20",
        firstNameStandardized = "firstNameStandardized",
        lastNameStandardized = "lastNameStandardized",
    )

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
