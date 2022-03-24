package de.rki.coronawarnapp.main

import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.coronatest.PersonalTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidPcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.main.MainActivityViewModel
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.preferences.mockFlowPreference
import java.util.Locale

@ExtendWith(InstantExecutorExtension::class)
class MainActivityViewModelTest2 : BaseTest() {

    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var diarySettings: ContactDiarySettings
    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var traceLocationSettings: TraceLocationSettings
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var coronTestRepository: PersonalTestRepository
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var tracingSettings: TracingSettings

    private val raExtractor = spyk(RapidAntigenQrCodeExtractor())
    private val rPcrExtractor = spyk(RapidPcrQrCodeExtractor())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.contactdiary.util.ContactDiaryExtensionsKt")

        mockkObject(CWADebug)

        every { onboardingSettings.isOnboarded } returns true
        every { onboardingSettings.fabScannerOnboardingDone } returns mockFlowPreference(true)
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.WRU
        every { traceLocationSettings.onboardingStatus } returns mockFlowPreference(
            TraceLocationSettings.OnboardingStatus.NOT_ONBOARDED
        )
        every { onboardingSettings.isBackgroundCheckDone } returns true
        every { checkInRepository.checkInsWithinRetention } returns MutableStateFlow(listOf())
        every { submissionRepository.testForType(any()) } returns flowOf()
        every { coronTestRepository.coronaTests } returns flowOf()
        every { valueSetsRepository.context } returns mockk()
        every { valueSetsRepository.context.getLocale() } returns Locale.GERMAN
        every { valueSetsRepository.triggerUpdateValueSet(any()) } just Runs

        personCertificatesProvider.apply {
            every { personCertificates } returns emptyFlow()
            every { personsBadgeCount } returns flowOf(0)
        }

        every { tracingSettings.showRiskLevelBadge } returns mockFlowPreference(false)
    }

    private fun createInstance(): MainActivityViewModel = MainActivityViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        environmentSetup = environmentSetup,
        backgroundModeStatus = backgroundModeStatus,
        contactDiarySettings = diarySettings,
        backgroundNoise = backgroundNoise,
        onboardingSettings = onboardingSettings,
        checkInRepository = checkInRepository,
        traceLocationSettings = traceLocationSettings,
        covidCertificateSettings = covidCertificateSettings,
        personCertificatesProvider = personCertificatesProvider,
        raExtractor = raExtractor,
        rPcrExtractor = rPcrExtractor,
        submissionRepository = submissionRepository,
        personalTestRepository = coronTestRepository,
        valueSetRepository = valueSetsRepository,
        tracingSettings = tracingSettings,
    )

    @Test
    fun `Home screen badge count shows tests badges only`() {
        val coronaTest = mockk<PersonalCoronaTest>().apply { every { didShowBadge } returns false }
        every { tracingSettings.showRiskLevelBadge } returns mockFlowPreference(false)
        every { coronTestRepository.coronaTests } returns flowOf(setOf(coronaTest))

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 1
    }

    @Test
    fun `Home screen badge count shows risk badges only`() {
        every { tracingSettings.showRiskLevelBadge } returns mockFlowPreference(true)
        every { coronTestRepository.coronaTests } returns flowOf(emptySet())

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 1
    }

    @Test
    fun `Home screen badge count shows risk + tests badges only`() {
        val coronaTest = mockk<PersonalCoronaTest>().apply { every { didShowBadge } returns false }
        every { tracingSettings.showRiskLevelBadge } returns mockFlowPreference(true)
        every { coronTestRepository.coronaTests } returns flowOf(setOf(coronaTest))

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 2
    }

    @Test
    fun `Home screen badge count shows risk + tests badges is ZERO`() {
        val coronaTest = mockk<PersonalCoronaTest>().apply { every { didShowBadge } returns true }
        every { tracingSettings.showRiskLevelBadge } returns mockFlowPreference(false)
        every { coronTestRepository.coronaTests } returns flowOf(setOf(coronaTest))

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 0
    }
}
