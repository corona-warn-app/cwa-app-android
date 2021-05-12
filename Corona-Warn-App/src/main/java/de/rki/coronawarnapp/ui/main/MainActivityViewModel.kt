package de.rki.coronawarnapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Suppress("LongParameterList")
class MainActivityViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val environmentSetup: EnvironmentSetup,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val contactDiarySettings: ContactDiarySettings,
    private val backgroundNoise: BackgroundNoise,
    private val onboardingSettings: OnboardingSettings,
    private val traceLocationSettings: TraceLocationSettings,
    checkInRepository: CheckInRepository
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val showEnvironmentHint = SingleLiveEvent<String>()

    val showBackgroundJobDisabledNotification = SingleLiveEvent<Unit>()
    val showEnergyOptimizedEnabledForBackground = SingleLiveEvent<Unit>()
    private val mutableIsContactDiaryOnboardingDone = MutableLiveData<Boolean>()
    val isContactDiaryOnboardingDone: LiveData<Boolean> = mutableIsContactDiaryOnboardingDone
    private val mutableIsTraceLocationOnboardingDone = MutableLiveData<Boolean>()
    val isTraceLocationOnboardingDone: LiveData<Boolean> = mutableIsTraceLocationOnboardingDone

    val activeCheckIns = checkInRepository.checkInsWithinRetention
        .map { checkins -> checkins.filter { !it.completed }.size }
        .asLiveData(context = dispatcherProvider.Default)

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
    }

    private suspend fun checkForEnergyOptimizedEnabled() {
        if (!backgroundModeStatus.isIgnoringBatteryOptimizations.first()) {
            showEnergyOptimizedEnabledForBackground.postValue(Unit)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<MainActivityViewModel>
}
