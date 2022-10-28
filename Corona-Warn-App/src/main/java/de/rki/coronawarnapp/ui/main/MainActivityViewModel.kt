package de.rki.coronawarnapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUiSettings
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidPcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.main.CWASettings.Companion.DEFAULT_APP_VERSION
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.qrcode.handler.CoronaTestQRCodeHandler
import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreEvent
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreHandler
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.ui.main.home.MainActivityEvent
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Suppress("LongParameterList")
class MainActivityViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val environmentSetup: EnvironmentSetup,
    private val backgroundModeStatus: BackgroundModeStatus,
    contactDiaryUiSettings: ContactDiaryUiSettings,
    private val onboardingSettings: OnboardingSettings,
    private val traceLocationSettings: TraceLocationSettings,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val raExtractor: RapidAntigenQrCodeExtractor,
    private val rPcrExtractor: RapidPcrQrCodeExtractor,
    private val coronaTestQRCodeHandler: CoronaTestQRCodeHandler,
    private val coronaTestRestoreHandler: CoronaTestRestoreHandler,
    coronaTestRepository: CoronaTestRepository,
    familyTestRepository: FamilyTestRepository,
    checkInRepository: CheckInRepository,
    personCertificatesProvider: PersonCertificatesProvider,
    valueSetRepository: ValueSetsRepository,
    tracingSettings: TracingSettings,
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val isToolTipVisible: LiveData<Boolean> = combine(
        onboardingSettings.fabScannerOnboardingDone,
        onboardingSettings.fabUqsLogVersion
    ) { done, version ->
        !done && version != DEFAULT_APP_VERSION && version < BuildConfigWrap.VERSION_CODE
    }.asLiveData2()
    val showEnvironmentHint = SingleLiveEvent<String>()
    val event = SingleLiveEvent<MainActivityEvent>()

    private val mutableCoronaTestResult = SingleLiveEvent<CoronaTestQRCodeHandler.Result>()
    val coronaTestResult: LiveData<CoronaTestQRCodeHandler.Result> = mutableCoronaTestResult

    private val mutableCoronaTestRestoreEvent = SingleLiveEvent<CoronaTestRestoreEvent>()
    val coronaTestRestoreEvent: LiveData<CoronaTestRestoreEvent> = mutableCoronaTestRestoreEvent

    val showBackgroundJobDisabledNotification = SingleLiveEvent<Unit>()
    val showEnergyOptimizedEnabledForBackground = SingleLiveEvent<Unit>()
    val isContactDiaryOnboardingDone: LiveData<Boolean> = contactDiaryUiSettings.isOnboardingDone.asLiveData2()
    private val mutableIsTraceLocationOnboardingDone = MutableLiveData<Boolean>()
    val isTraceLocationOnboardingDone: LiveData<Boolean> = mutableIsTraceLocationOnboardingDone
    private val mutableIsCertificatesOnboardingDone = MutableLiveData<Boolean>()
    val isCertificatesConsentGiven: LiveData<Boolean> = mutableIsCertificatesOnboardingDone

    val activeCheckIns = checkInRepository.checkInsWithinRetention
        .map { checkins -> checkins.filter { !it.completed }.size }
        .asLiveData2()

    val personsBadgeCount: LiveData<Int> = personCertificatesProvider.personsBadgeCount.asLiveData2()

    val mainBadgeCount: LiveData<Int> = combine(
        coronaTestRepository.coronaTests,
        familyTestRepository.familyTests,
        tracingSettings.showRiskLevelBadge
    ) { personalTests, familyTests, showBadge ->
        personalTests.plus(familyTests).count { it.hasBadge }.plus(if (showBadge) 1 else 0)
    }.asLiveData2()

    init {
        if (CWADebug.isDeviceForTestersBuild) {
            launch {
                val current = if (environmentSetup.launchEnvironment != null)
                    "base64 data"
                else
                    environmentSetup.currentEnvironment.rawKey

                if (current != EnvironmentSetup.Type.PRODUCTION.rawKey) {
                    showEnvironmentHint.postValue(current)
                }
            }
        }

        valueSetRepository.triggerUpdateValueSet()

        launch {
            if (!onboardingSettings.isBackgroundCheckDone.first()) {
                onboardingSettings.updateBackgroundCheckDone(isDone = true)
                if (backgroundModeStatus.isBackgroundRestricted.first()) {
                    showBackgroundJobDisabledNotification.postValue(Unit)
                } else {
                    checkForEnergyOptimizedEnabled()
                }
            }
        }
    }

    fun onUserOpenedBackgroundPriorityOptions() {
        launch {
            checkForEnergyOptimizedEnabled()
        }
    }

    fun onBottomNavSelected() = launch {
        mutableIsTraceLocationOnboardingDone.postValue(traceLocationSettings.isOnboardingDone())
        covidCertificateSettings.isOnboarded.first().let {
            mutableIsCertificatesOnboardingDone.postValue(it)
        }
    }

    private suspend fun checkForEnergyOptimizedEnabled() {
        if (!backgroundModeStatus.isIgnoringBatteryOptimizations.first()) {
            showEnergyOptimizedEnabledForBackground.postValue(Unit)
        }
    }

    fun onNavigationUri(uriString: String) = launch {
        when {
            CheckInsFragment.canHandle(uriString) -> event.postValue(
                MainActivityEvent.GoToCheckInsFragment(uriString)
            )
            raExtractor.canHandle(uriString) -> raExtractor.handleCoronaTestQr(uriString = uriString)
            rPcrExtractor.canHandle(uriString) -> rPcrExtractor.handleCoronaTestQr(uriString = uriString)
        }
    }

    private suspend fun QrCodeExtractor<CoronaTestQRCode>.handleCoronaTestQr(uriString: String) = try {
        val qrCode = extract(rawString = uriString)
        val coronaTestResult = coronaTestQRCodeHandler.handleQrCode(qrCode)
        mutableCoronaTestResult.postValue(coronaTestResult)
    } catch (e: Exception) {
        Timber.w(e, "onNavigationUri failed")
        event.postValue(MainActivityEvent.Error(e))
    }

    fun restoreCoronaTest(recycledCoronaTest: BaseCoronaTest) = launch {
        val coronaTestRestoreEvent = coronaTestRestoreHandler.restoreCoronaTest(recycledCoronaTest, openResult = false)
        mutableCoronaTestRestoreEvent.postValue(coronaTestRestoreEvent)
    }

    fun openScanner() = launch {
        event.postValue(MainActivityEvent.OpenScanner)
    }

    fun dismissTooltip() = launch {
        onboardingSettings.updateFabScannerOnboardingDone(isDone = true)
        onboardingSettings.updateFabUqsVersion(BuildConfigWrap.VERSION_CODE)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<MainActivityViewModel>
}
