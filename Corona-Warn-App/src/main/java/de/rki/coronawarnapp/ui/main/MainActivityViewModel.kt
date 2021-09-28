package de.rki.coronawarnapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.coronatest.qrcode.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.main.home.MainActivityEvent
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.permission.CameraPermissionProvider
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Suppress("LongParameterList")
class MainActivityViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val environmentSetup: EnvironmentSetup,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val contactDiarySettings: ContactDiarySettings,
    private val backgroundNoise: BackgroundNoise,
    private val onboardingSettings: OnboardingSettings,
    private val traceLocationSettings: TraceLocationSettings,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val raExtractor: RapidAntigenQrCodeExtractor,
    private val submissionRepository: SubmissionRepository,
    private val cameraPermissionProvider: CameraPermissionProvider,
    coronaTestRepository: CoronaTestRepository,
    checkInRepository: CheckInRepository,
    personCertificatesProvider: PersonCertificatesProvider,
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val isToolTipVisible: LiveData<Boolean> = onboardingSettings.fabScannerOnboardingDone.flow.map { done ->
        !done
    }.asLiveData2()
    val showEnvironmentHint = SingleLiveEvent<String>()
    val event = SingleLiveEvent<MainActivityEvent>()

    val showBackgroundJobDisabledNotification = SingleLiveEvent<Unit>()
    val showEnergyOptimizedEnabledForBackground = SingleLiveEvent<Unit>()
    private val mutableIsContactDiaryOnboardingDone = MutableLiveData<Boolean>()
    val isContactDiaryOnboardingDone: LiveData<Boolean> = mutableIsContactDiaryOnboardingDone
    private val mutableIsTraceLocationOnboardingDone = MutableLiveData<Boolean>()
    val isTraceLocationOnboardingDone: LiveData<Boolean> = mutableIsTraceLocationOnboardingDone
    private val mutableIsVaccinationOnboardingDone = MutableLiveData<Boolean>()
    val isVaccinationConsentGiven: LiveData<Boolean> = mutableIsVaccinationOnboardingDone

    val activeCheckIns = checkInRepository.checkInsWithinRetention
        .map { checkins -> checkins.filter { !it.completed }.size }
        .asLiveData2()

    val personsBadgeCount: LiveData<Int> = personCertificatesProvider.personsBadgeCount.asLiveData2()

    val testsBadgeCount: LiveData<Int> = coronaTestRepository.coronaTests
        .map { coronaTests ->
            coronaTests.filter { !it.didShowBadge }.count()
        }.asLiveData2()

    init {
        if (CWADebug.isDeviceForTestersBuild) {
            launch {
                val current = environmentSetup.currentEnvironment
                if (current != EnvironmentSetup.Type.PRODUCTION) {
                    showEnvironmentHint.postValue(current.rawKey)
                }
            }
        }

        launch {
            if (!onboardingSettings.isBackgroundCheckDone) {
                onboardingSettings.isBackgroundCheckDone = true
                if (backgroundModeStatus.isBackgroundRestricted.first()) {
                    showBackgroundJobDisabledNotification.postValue(Unit)
                } else {
                    checkForEnergyOptimizedEnabled()
                }
            }
        }
    }

    fun doBackgroundNoiseCheck() {
        launch {
            backgroundNoise.foregroundScheduleCheck()
        }
    }

    fun onUserOpenedBackgroundPriorityOptions() {
        launch {
            checkForEnergyOptimizedEnabled()
        }
    }

    fun onBottomNavSelected() {
        mutableIsContactDiaryOnboardingDone.value = contactDiarySettings.isOnboardingDone
        mutableIsTraceLocationOnboardingDone.value = traceLocationSettings.isOnboardingDone
        mutableIsVaccinationOnboardingDone.value = covidCertificateSettings.isOnboarded.value
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
            raExtractor.canHandle(uriString) -> {
                try {
                    val qrCode = raExtractor.extract(rawString = uriString)
                    val test = submissionRepository.testForType(qrCode.type).first()
                    when {
                        test != null -> event.postValue(MainActivityEvent.GoToDeletionScreen(qrCode))
                        else -> event.postValue(MainActivityEvent.GoToSubmissionConsentFragment(qrCode))
                    }
                } catch (e: Exception) {
                    Timber.w(e, "onNavigationUri failed")
                    event.postValue(MainActivityEvent.Error(e))
                }
            }
        }
    }

    fun openScanner() = launch {
        event.postValue(MainActivityEvent.OpenScanner(cameraPermissionProvider.deniedPermanently.first()))
    }

    fun dismissTooltip() {
        onboardingSettings.fabScannerOnboardingDone.update { false }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<MainActivityViewModel>
}
