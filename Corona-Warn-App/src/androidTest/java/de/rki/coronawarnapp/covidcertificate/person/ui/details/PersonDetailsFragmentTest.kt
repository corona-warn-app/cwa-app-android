package de.rki.coronawarnapp.covidcertificate.person.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.MaskState
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.AdmissionStatusCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.BoosterCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateReissuanceCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.MaskRequirementsCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationInfoCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.toLocalDateUserTz
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import testhelpers.takeScreenshot
import java.time.Instant
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
                    groupKey: String,
                    colorShade: PersonColorShade
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
        every { viewModel.uiState } returns certificateData(true, withoutMask = null)

        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("cwa")

        onView(withId(R.id.coordinator_layout)).perform(swipeUp())
        takeScreenshot<PersonDetailsFragment>("cwa_2")
    }

    @Test
    @Screenshot
    fun capture_fragment_not_cwa_user() {
        every { viewModel.uiState } returns certificateData(withoutMask = null)
        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("not_cwa")
    }

    @Test
    @Screenshot
    fun capture_fragment_admission_berlin() {
        every { viewModel.uiState } returns certificateData(admissionSubtitle = "Berlin", withoutMask = null)
        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("admission_berlin")
    }

    @Test
    @Screenshot
    fun capture_fragment_without_mask() {
        every { viewModel.currentColorShade } returns MutableLiveData(PersonColorShade.GREEN)
        every { viewModel.uiState } returns certificateData(withoutMask = true)
        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("no_mask_required")
    }

    @Test
    @Screenshot
    fun capture_fragment_with_mask() {
        every { viewModel.uiState } returns certificateData(withoutMask = false)
        launchFragmentInContainer2<PersonDetailsFragment>(fragmentArgs = args)
        takeScreenshot<PersonDetailsFragment>("no_mask_required")
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

    private fun certificateData(
        isCwa: Boolean = false,
        admissionSubtitle: String = "2G+ PCR-Test",
        withoutMask: Boolean?
    ): LiveData<PersonDetailsViewModel.UiState> {
        var name: String
        var colorShade = PersonColorShade.COLOR_1
        val certificateItems = mutableListOf<CertificateItem>().apply {
            val testCertificate = mockTestCertificate().also { name = it.fullName }
            val vaccinationCertificate1 = mockVaccinationCertificate(number = 1, final = false)
            val vaccinationCertificate2 = mockVaccinationCertificate(number = 2, final = true)
            val vaccinationCertificate3 = mockVaccinationCertificate(number = 3, final = false, booster = true)
            val recoveryCertificate = mockRecoveryCertificate()
            val personCertificates = PersonCertificates(
                listOf(testCertificate, vaccinationCertificate1, vaccinationCertificate2, vaccinationCertificate3),
                isCwaUser = isCwa
            )

            if (withoutMask != null) {
                add(
                    if (withoutMask) {
                        colorShade = PersonColorShade.GREEN
                        MaskRequirementsCard.Item(
                            titleText = "Keine Maskenpflicht",
                            subtitleText = "Eine Maske ist dennoch empfohlen",
                            maskStateIdentifier = MaskState.MaskStateIdentifier.OPTIONAL,
                            longText = "Von der Maskenpflicht sind alle Personen befreit, die innerhalb der letzten 3 Monate geimpft wurden oder genesen sind oder innerhalb der letzten 24 Stunden negativ getestet wurden.",
                            faqAnchor = "FAQ",
                            colorShade = PersonColorShade.GREEN
                        )
                    } else {
                        MaskRequirementsCard.Item(
                            titleText = "Maskenpflicht",
                            subtitleText = "Sie sind nicht von der Maskenpflicht ausgenommen",
                            maskStateIdentifier = MaskState.MaskStateIdentifier.REQUIRED,
                            longText = "Von der Maskenpflicht sind alle Personen befreit, die innerhalb der letzten 3 Monate geimpft wurden oder genesen sind oder innerhalb der letzten 24 Stunden negativ getestet wurden.",
                            faqAnchor = "FAQ",
                            colorShade = PersonColorShade.COLOR_1
                        )
                    }
                )
            }

            add(
                when (Locale.getDefault()) {
                    Locale.GERMANY, Locale.GERMAN -> AdmissionStatusCard.Item(
                        colorShade = colorShade,
                        titleText = "Status-Nachweis",
                        subtitleText = admissionSubtitle,
                        badgeText = "2G+",
                        longText = "Ihre Zertifikate erfüllen die 2G-Plus-Regel. Wenn Sie Ihren aktuellen Status vorweisen müssen, schließen Sie diese Ansicht und zeigen Sie den QR-Code auf der Zertifikatsübersicht.",
                        faqAnchor = "FAQ",
                        longTextWithBadge = "Ihr Status hat sich geändert. Ihre Zertifikate erfüllen jetzt die 2G-Regel. Wenn Sie Ihren aktuellen Status vorweisen müssen, schließen Sie diese Ansicht und zeigen Sie den QR-Code auf der Zertifikatsübersicht."
                    )
                    else -> AdmissionStatusCard.Item(
                        colorShade = colorShade,
                        titleText = "Proof of Status",
                        subtitleText = admissionSubtitle,
                        badgeText = "2G+",
                        longText = "Your certificates satisfy the 2G plus rule. If you need to prove your current status, close this view and show the QR code in the certificate overview.",
                        faqAnchor = "FAQ",
                        longTextWithBadge = "Your status has changed. Your certificates now comply with the 2G rule. If you need to show your current status, close this view and show the QR code on the certificate overview."
                    )
                }
            )

            val (title, subtitle) = if (Locale.getDefault().language == Locale.GERMAN.language) {
                "Zertifikat aktualisieren" to "Neuausstellung direkt über die App vornehmen"
            } else {
                "Update certificate" to "Reissue directly via the app"
            }

            add(
                CertificateReissuanceCard.Item(
                    title = title,
                    subtitle = subtitle,
                    badgeVisible = true,
                    onClick = {}
                )
            )

            add(
                when (Locale.getDefault()) {
                    Locale.GERMANY, Locale.GERMAN ->
                        VaccinationInfoCard.Item(
                            titleText = "Impfstatus",
                            subtitleText = "Letzte Impfung vor 14 Tagen",
                            longText = "Sie haben nun alle derzeit geplanten Impfungen erhalten. Ihr Impfschutz ist vollständig.",
                            faqAnchor = "FAQ"
                        )
                    else -> VaccinationInfoCard.Item(
                        titleText = "Vaccination Status",
                        subtitleText = "Last vaccination 14 days ago",
                        longText = "You have received all currently planned vaccinations. Your vaccination protection is complete.",
                        faqAnchor = "FAQ"
                    )
                }
            )

            add(CwaUserCard.Item(personCertificates) {})
            add(
                VaccinationCertificateCard.Item(
                    certificate = vaccinationCertificate1,
                    isCurrentCertificate = false,
                    colorShade = PersonColorShade.COLOR_1,
                    validateCertificate = {},
                    onClick = {},
                    onSwipeItem = { _, _ -> }
                )
            )
            add(
                VaccinationCertificateCard.Item(
                    certificate = vaccinationCertificate2,
                    isCurrentCertificate = false,
                    colorShade = PersonColorShade.COLOR_1,
                    validateCertificate = {},
                    onClick = {},
                    onSwipeItem = { _, _ -> }
                )
            )

            add(
                VaccinationCertificateCard.Item(
                    certificate = vaccinationCertificate3,
                    isCurrentCertificate = false,
                    colorShade = PersonColorShade.COLOR_1,
                    validateCertificate = {},
                    onClick = {},
                    onSwipeItem = { _, _ -> }
                )
            )

            add(
                TestCertificateCard.Item(
                    certificate = testCertificate,
                    isCurrentCertificate = true,
                    colorShade = PersonColorShade.COLOR_1,
                    validateCertificate = {},
                    onClick = {},
                    onSwipeItem = { _, _ -> }
                )
            )
            add(
                RecoveryCertificateCard.Item(
                    certificate = recoveryCertificate,
                    isCurrentCertificate = false,
                    colorShade = PersonColorShade.COLOR_1,
                    validateCertificate = {},
                    onClick = {},
                    onSwipeItem = { _, _ -> }
                )
            )
        }

        return MutableLiveData(PersonDetailsViewModel.UiState(name, certificateItems))
    }

    private fun boosterCertificateData(isCwa: Boolean = false): LiveData<PersonDetailsViewModel.UiState> {
        var name: String
        val certificateItems = mutableListOf<CertificateItem>().apply {
            val vaccinationCertificate1 =
                mockVaccinationCertificate(number = 3, final = false, booster = true).also { name = it.fullName }

            every { vaccinationCertificate1.hasNotificationBadge } returns true

            val personCertificates = PersonCertificates(
                listOf(vaccinationCertificate1),
                isCwaUser = isCwa
            )

            add(
                when (Locale.getDefault()) {
                    Locale.GERMANY, Locale.GERMAN -> AdmissionStatusCard.Item(
                        colorShade = PersonColorShade.COLOR_1,
                        titleText = "Status-Nachweis",
                        subtitleText = "2G+ PCR-Test",
                        badgeText = "2G+",
                        longText = "Ihre Zertifikate erfüllen die 2G-Plus-Regel. Wenn Sie Ihren aktuellen Status vorweisen müssen, schließen Sie diese Ansicht und zeigen Sie den QR-Code auf der Zertifikatsübersicht.",
                        faqAnchor = "FAQ",
                        longTextWithBadge = "Ihr Status hat sich geändert. Ihre Zertifikate erfüllen jetzt die 2G-Regel. Wenn Sie Ihren aktuellen Status vorweisen müssen, schließen Sie diese Ansicht und zeigen Sie den QR-Code auf der Zertifikatsübersicht."
                    )
                    else -> AdmissionStatusCard.Item(
                        colorShade = PersonColorShade.COLOR_1,
                        titleText = "Proof of Status",
                        subtitleText = "2G+ PCR-Test",
                        badgeText = "2G+",
                        longText = "Your certificates satisfy the 2G plus rule. If you need to prove your current status, close this view and show the QR code in the certificate overview.",
                        faqAnchor = "FAQ",
                        longTextWithBadge = "Your status has changed. Your certificates now comply with the 2G rule. If you need to show your current status, close this view and show the QR code on the certificate overview."
                    )
                }
            )

            val subtitle = if (Locale.getDefault().language == Locale.GERMAN.language) {
                "Sie könnten für eine Auffrischungsimpfung berechtigt sein, da Sie vor mehr als 4 Monaten von COVID-19 genesen sind trotz einer vorherigen Impfung."
            } else {
                "You may be eligible for a booster because you recovered from COVID-19 more than 4 months ago despite a prior vaccination."
            }

            add(
                BoosterCard.Item(
                    title = "Booster",
                    subtitle = subtitle,
                    badgeVisible = true,
                    onClick = {}
                )
            )

            add(
                when (Locale.getDefault()) {
                    Locale.GERMANY, Locale.GERMAN ->
                        VaccinationInfoCard.Item(
                            titleText = "Impfstatus",
                            subtitleText = "Letzte Impfung vor 14 Tagen",
                            longText = "Sie haben nun alle derzeit geplanten Impfungen erhalten. Ihr Impfschutz ist vollständig.",
                            faqAnchor = "FAQ"
                        )
                    else -> VaccinationInfoCard.Item(
                        titleText = "Vaccination Status",
                        subtitleText = "Last vaccination 14 days ago",
                        longText = "You have received all currently planned vaccinations. Your vaccination protection is complete.",
                        faqAnchor = "FAQ"
                    )
                }
            )

            add(CwaUserCard.Item(personCertificates) {})
            add(
                VaccinationCertificateCard.Item(
                    certificate = vaccinationCertificate1,
                    isCurrentCertificate = true,
                    colorShade = PersonColorShade.COLOR_1,
                    validateCertificate = {},
                    onClick = {},
                    onSwipeItem = { _, _ -> }
                )
            )
        }

        return MutableLiveData(PersonDetailsViewModel.UiState(name, certificateItems))
    }

    private fun mockTestCertificate(): TestCertificate = mockk<TestCertificate>().apply {
        every { uniqueCertificateIdentifier } returns "RN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
        every { fullName } returns "Andrea Schneider"
        every { rawCertificate } returns mockk<TestDccV1>().apply {
            every { test } returns mockk<DccV1.TestCertificateData>().apply {
                every { testType } returns "LP6464-4"
                every { sampleCollectedAt } returns Instant.parse("2021-05-31T11:35:00.000Z")
            }
        }
        every { headerExpiresAt } returns Instant.now().plusMillis(20)
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
        every { isDisplayValid } returns true
        every { sampleCollectedAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
        every { state } returns CwaCovidCertificate.State.Valid(headerExpiresAt)
        every { isNew } returns false
        every { hasNotificationBadge } returns false
        every { isNotScreened } returns true
        every { qrCodeHash } returns "TC"
        every { isPCRTestCertificate } returns true
    }

    private fun mockVaccinationCertificate(
        number: Int = 1,
        final: Boolean = false,
        booster: Boolean = false
    ): VaccinationCertificate =
        mockk<VaccinationCertificate>().apply {
            val localDate = Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
            every { fullName } returns "Andrea Schneider"
            every { uniqueCertificateIdentifier } returns
                "RN:UVCI:01:AT:858CC${number}8CFCF5965EF82F60E493349AA5#K"
            every { rawCertificate } returns mockk<VaccinationDccV1>().apply {
                every { vaccination } returns mockk<DccV1.VaccinationData>().apply {
                    every { doseNumber } returns number
                    every { totalSeriesOfDoses } returns 2
                    every { vaccinatedOn } returns localDate
                    every { medicalProductId } returns "medicalProductId"
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
            every { totalSeriesOfDoses } returns if (booster) number else 2
            every { dateOfBirthFormatted } returns "1981-03-20"
            every { isSeriesCompletingShot } returns final
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
            every { isDisplayValid } returns true
            every { state } returns CwaCovidCertificate.State.Valid(Instant.now().plusMillis(20))
            every { hasNotificationBadge } returns false
            every { isNew } returns false
            every { isNotScreened } returns true
            every { qrCodeHash } returns "VC$number"
            every { headerIssuedAt } returns Instant.parse("2021-06-01T11:35:00.000Z")
        }

    private fun mockRecoveryCertificate(): RecoveryCertificate =
        mockk<RecoveryCertificate>().apply {
            every { fullName } returns "Andrea Schneider"
            every { uniqueCertificateIdentifier } returns "RN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
            every { dateOfBirthFormatted } returns "1981-03-20"
            every { testedPositiveOn } returns Instant.parse("2021-05-23T11:35:00.000Z").toLocalDateUserTz()
            every { personIdentifier } returns certificatePersonIdentifier
            every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.recoveryCertificate)
            every { containerId } returns rcContainerId
            every { isDisplayValid } returns true
            every { state } returns CwaCovidCertificate.State.Valid(Instant.now().plusMillis(20))
            every { hasNotificationBadge } returns false
            every { isNew } returns false
            every { isNotScreened } returns true
            every { qrCodeHash } returns "RC"
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
