package de.rki.coronawarnapp.main

import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUiSettings
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidPcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.qrcode.handler.CoronaTestQRCodeHandler
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreEvent
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreHandler
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.ui.main.MainActivityViewModel
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
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
import java.time.Instant
import java.util.Locale

@ExtendWith(InstantExecutorExtension::class)
class MainActivityViewModelTest2 : BaseTest() {

    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var diarySettings: ContactDiaryUiSettings
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var traceLocationSettings: TraceLocationSettings
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var coronTestRepository: CoronaTestRepository
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var coronaTestQRCodeHandler: CoronaTestQRCodeHandler
    @MockK lateinit var coronaTestRestoreHandler: CoronaTestRestoreHandler
    @MockK lateinit var familyTestRepository: FamilyTestRepository

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
        every { traceLocationSettings.onboardingStatus } returns
            flowOf(TraceLocationSettings.OnboardingStatus.NOT_ONBOARDED)
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

        every { diarySettings.isOnboardingDone } returns flowOf(false)
    }

    private fun createInstance(): MainActivityViewModel = MainActivityViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        environmentSetup = environmentSetup,
        backgroundModeStatus = backgroundModeStatus,
        contactDiaryUiSettings = diarySettings,
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
    fun `Home screen badge count shows tests badges only`() {
        val coronaTest = mockk<PersonalCoronaTest>().apply { every { hasBadge } returns true }
        val familyCoronaTest = mockk<FamilyCoronaTest>().apply { every { hasBadge } returns true }
        every { tracingSettings.showRiskLevelBadge } returns flowOf(false)
        every { coronTestRepository.coronaTests } returns flowOf(setOf(coronaTest))
        every { familyTestRepository.familyTests } returns flowOf(setOf(familyCoronaTest))

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 2
    }

    @Test
    fun `Home screen badge count shows risk badges only`() {
        every { tracingSettings.showRiskLevelBadge } returns flowOf(true)
        every { coronTestRepository.coronaTests } returns flowOf(emptySet())

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 1
    }

    @Test
    fun `Home screen badge count shows risk + tests badges only`() {
        val coronaTest = mockk<PersonalCoronaTest>().apply { every { hasBadge } returns true }
        val familyCoronaTest = mockk<FamilyCoronaTest>().apply { every { hasBadge } returns true }

        every { tracingSettings.showRiskLevelBadge } returns flowOf(true)
        every { coronTestRepository.coronaTests } returns flowOf(setOf(coronaTest))
        every { familyTestRepository.familyTests } returns flowOf(setOf(familyCoronaTest))

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 3
    }

    @Test
    fun `Home screen badge count shows risk + tests badges is ZERO`() {
        val coronaTest = mockk<PersonalCoronaTest>().apply { every { hasBadge } returns false }
        every { tracingSettings.showRiskLevelBadge } returns flowOf(false)
        every { coronTestRepository.coronaTests } returns flowOf(setOf(coronaTest))

        createInstance().mainBadgeCount.getOrAwaitValue() shouldBe 0
    }

    @Test
    fun `onNavigationUri - R-PCR test uri string`() {
        val coronaTestQrCode = CoronaTestQRCode.RapidPCR(
            rawQrCode = "rawQrCode",
            hash = "hash",
            createdAt = Instant.EPOCH
        )
        val uriString = "R-PCR uri string"
        val result = CoronaTestQRCodeHandler.TestRegistrationSelection(coronaTestQrCode)

        coEvery { rPcrExtractor.canHandle(uriString) } returns true
        coEvery { rPcrExtractor.extract(uriString) } returns coronaTestQrCode
        coEvery { coronaTestQRCodeHandler.handleQrCode(coronaTestQrCode) } returns result

        with(createInstance()) {
            onNavigationUri(uriString)

            coronaTestResult.getOrAwaitValue() shouldBe result
        }

        coVerify {
            coronaTestQRCodeHandler.handleQrCode(coronaTestQrCode)
        }
    }

    @Test
    fun `onNavigationUri - RAT test uri string`() {
        val coronaTestQrCode = CoronaTestQRCode.RapidAntigen(
            rawQrCode = "rawQrCode",
            hash = "hash",
            createdAt = Instant.EPOCH
        )
        val uriString = "RAT uri string"
        val result = CoronaTestQRCodeHandler.TestRegistrationSelection(coronaTestQrCode)

        coEvery { raExtractor.canHandle(uriString) } returns true
        coEvery { raExtractor.extract(uriString) } returns coronaTestQrCode
        coEvery { coronaTestQRCodeHandler.handleQrCode(coronaTestQrCode) } returns result

        with(createInstance()) {
            onNavigationUri(uriString)

            coronaTestResult.getOrAwaitValue() shouldBe result
        }

        coVerify {
            coronaTestQRCodeHandler.handleQrCode(coronaTestQrCode)
        }
    }

    @Test
    fun `restoreCoronaTest calls CoronaTestRestoreHandler`() {
        val recycledPCR = PCRCoronaTest(
            identifier = "pcr-identifier",
            lastUpdatedAt = Instant.EPOCH,
            registeredAt = Instant.EPOCH,
            registrationToken = "token",
            testResult = de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_NEGATIVE,
            isDccConsentGiven = true
        )
        val request = recycledPCR.toRestoreRecycledTestRequest()
        val restoreEvent = CoronaTestRestoreEvent.RestoreDuplicateTest(restoreRecycledTestRequest = request)
        coEvery { coronaTestRestoreHandler.restoreCoronaTest(recycledPCR, openResult = false) } returns restoreEvent

        with(createInstance()) {
            restoreCoronaTest(recycledPCR)
            coronaTestRestoreEvent.getOrAwaitValue() shouldBe restoreEvent

            val restoreEvent2 = CoronaTestRestoreEvent.RestoredTest(recycledPCR)
            coEvery {
                coronaTestRestoreHandler.restoreCoronaTest(
                    recycledPCR,
                    openResult = false
                )
            } returns restoreEvent2
            restoreCoronaTest(recycledPCR)
            coronaTestRestoreEvent.getOrAwaitValue() shouldBe restoreEvent2
        }

        coVerify {
            coronaTestRestoreHandler.restoreCoronaTest(recycledPCR, openResult = false)
        }
    }
}
