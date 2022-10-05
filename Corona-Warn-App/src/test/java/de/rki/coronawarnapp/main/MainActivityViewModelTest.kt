package de.rki.coronawarnapp.main

import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettingsStorage
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUiSettings
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidPcrQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.qrcode.handler.CoronaTestQRCodeHandler
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreHandler
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.ui.main.MainActivityViewModel
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
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
import java.util.Locale

@ExtendWith(InstantExecutorExtension::class)
class MainActivityViewModelTest : BaseTest() {

    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var traceLocationSettings: TraceLocationSettings
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var coronTestRepository: CoronaTestRepository
    @MockK lateinit var familyTestRepository: FamilyTestRepository
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var coronaTestQRCodeHandler: CoronaTestQRCodeHandler
    @MockK lateinit var coronaTestRestoreHandler: CoronaTestRestoreHandler
    @RelaxedMockK lateinit var contactDiarySettingsStorage: ContactDiarySettingsStorage

    private val raExtractor = spyk(RapidAntigenQrCodeExtractor())
    private val rPcrExtractor = spyk(RapidPcrQrCodeExtractor())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.contactdiary.util.ContactDiaryExtensionsKt")

        mockkObject(CWADebug)

        coEvery { onboardingSettings.isOnboarded() } returns true
        every { onboardingSettings.fabScannerOnboardingDone } returns flowOf(true)
        every { onboardingSettings.fabUqsLogVersion } returns flowOf(0)
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.WRU
        every { environmentSetup.launchEnvironment } returns null
        every { traceLocationSettings.onboardingStatus } returns
            flowOf(TraceLocationSettings.OnboardingStatus.NOT_ONBOARDED)
        coEvery { traceLocationSettings.isOnboardingDone() } returns false
        every { onboardingSettings.isBackgroundCheckDone } returns flowOf(true)
        every { checkInRepository.checkInsWithinRetention } returns MutableStateFlow(listOf())
        every { coronTestRepository.coronaTests } returns flowOf()
        every { valueSetsRepository.context } returns mockk()
        every { valueSetsRepository.context.getLocale() } returns Locale.GERMAN
        every { valueSetsRepository.triggerUpdateValueSet(any()) } just Runs

        personCertificatesProvider.apply {
            every { personCertificates } returns emptyFlow()
            every { personsBadgeCount } returns flowOf(0)
        }

        every { tracingSettings.showRiskLevelBadge } returns flowOf(false)
        every { familyTestRepository.familyTests } returns flowOf(setOf())

        every { contactDiarySettingsStorage.contactDiarySettings } returns createContactDiarySettingsFlow(
            onboardingStatus = ContactDiarySettings.OnboardingStatus.NOT_ONBOARDED
        )
    }

    private fun createInstance(): MainActivityViewModel = MainActivityViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        environmentSetup = environmentSetup,
        backgroundModeStatus = backgroundModeStatus,
        contactDiaryUiSettings = ContactDiaryUiSettings(contactDiarySettingsStorage = contactDiarySettingsStorage),
        onboardingSettings = onboardingSettings,
        checkInRepository = checkInRepository,
        traceLocationSettings = traceLocationSettings,
        covidCertificateSettings = covidCertificateSettings,
        personCertificatesProvider = personCertificatesProvider,
        raExtractor = raExtractor,
        rPcrExtractor = rPcrExtractor,
        coronaTestRepository = coronTestRepository,
        valueSetRepository = valueSetsRepository,
        tracingSettings = tracingSettings,
        coronaTestQRCodeHandler = coronaTestQRCodeHandler,
        coronaTestRestoreHandler = coronaTestRestoreHandler,
        familyTestRepository = familyTestRepository,
    )

    @Test
    fun `environment toast is visible test environments`() {
        every { CWADebug.isDeviceForTestersBuild } returns true
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.DEV

        val vm = createInstance()
        vm.showEnvironmentHint.value shouldBe EnvironmentSetup.Type.DEV.rawKey
    }

    @Test
    fun `environment toast is only visible in deviceForTesters flavor`() {
        every { CWADebug.isDeviceForTestersBuild } returns false
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.DEV

        val vm = createInstance()
        vm.showEnvironmentHint.value shouldBe null
    }

    @Test
    fun `environment toast is not visible in production`() {
        every { CWADebug.isDeviceForTestersBuild } returns true
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.PRODUCTION

        val vm = createInstance()
        vm.showEnvironmentHint.value shouldBe null
    }

    @Test
    fun `value set update is triggered on initialisation`() {
        createInstance()
        verify(exactly = 1) { valueSetsRepository.triggerUpdateValueSet(Locale.GERMAN) }
    }

    @Test
    fun `User is not onboarded when settings returns NOT_ONBOARDED `() {
        every { covidCertificateSettings.isOnboarded } returns flowOf(true)
        val vm = createInstance()
        vm.onBottomNavSelected()
        vm.isContactDiaryOnboardingDone.getOrAwaitValue() shouldBe false
    }

    @Test
    fun `User is onboarded when settings returns RISK_STATUS_1_12 `() {
        every { contactDiarySettingsStorage.contactDiarySettings } returns createContactDiarySettingsFlow(
            onboardingStatus = ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
        )
        every { covidCertificateSettings.isOnboarded } returns flowOf(false)
        val vm = createInstance()
        vm.onBottomNavSelected()
        vm.isContactDiaryOnboardingDone.getOrAwaitValue() shouldBe true
    }

    @Test
    fun `Vaccination is not acknowledged when settings returns false `() {
        every { covidCertificateSettings.isOnboarded } returns flowOf(false)
        val vm = createInstance()
        vm.onBottomNavSelected()
        vm.isCertificatesConsentGiven.value shouldBe false
    }

    @Test
    fun `Vaccination is acknowledged  when settings returns true `() {
        every { covidCertificateSettings.isOnboarded } returns flowOf(true)
        val vm = createInstance()
        vm.onBottomNavSelected()
        vm.isCertificatesConsentGiven.value shouldBe true
    }

    private fun createContactDiarySettingsFlow(
        onboardingStatus: ContactDiarySettings.OnboardingStatus
    ) = flowOf(ContactDiarySettings(onboardingStatus = onboardingStatus))
}
