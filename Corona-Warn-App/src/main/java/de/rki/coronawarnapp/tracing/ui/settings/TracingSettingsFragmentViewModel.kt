package de.rki.coronawarnapp.tracing.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.installTime.InstallTimeProvider
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.nearby.modules.tracing.disableTracingIfEnabled
import de.rki.coronawarnapp.risk.execution.ExposureWindowRiskWorkScheduler
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.ui.details.items.periodlogged.PeriodLoggedBox
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TracingSettingsFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingStatus: GeneralTracingStatus,
    installTimeProvider: InstallTimeProvider,
    appConfigProvider: AppConfigProvider,
    private val backgroundStatus: BackgroundModeStatus,
    tracingPermissionHelperFactory: TracingPermissionHelper.Factory,
    private val exposureWindowRiskWorkScheduler: ExposureWindowRiskWorkScheduler,
    private val enfClient: ENFClient,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val loggingPeriod: LiveData<PeriodLoggedBox.Item> = combine(
        tracingStatus.generalStatus,
        appConfigProvider.currentConfig
    ) { status,
        appConfig ->
        PeriodLoggedBox.Item(
            daysSinceInstallation = installTimeProvider.daysSinceInstallation,
            tracingStatus = status,
            maxEncounterAgeInDays = appConfig.maxEncounterAgeInDays
        )
    }
        .asLiveData(dispatcherProvider.Main)

    val tracingSettingsState: LiveData<TracingSettingsState> = tracingStatus.generalStatus
        .map { it.toTracingSettingsState() }
        .shareLatest(
            tag = "tracingSettingsState",
            scope = viewModelScope + dispatcherProvider.Default,
            started = SharingStarted.Eagerly
        )
        .asLiveData(dispatcherProvider.Main)

    val events = SingleLiveEvent<Event>()

    val isTracingSwitchChecked = MediatorLiveData<Boolean>().apply {
        addSource(tracingSettingsState) {
            value = it.isTracingSwitchChecked()
        }
    }

    val ensErrorEvents = SingleLiveEvent<Throwable>()

    private val tracingPermissionHelper =
        tracingPermissionHelperFactory.create(
            object : TracingPermissionHelper.Callback {
                override fun onUpdateTracingStatus(isTracingEnabled: Boolean) {
                    launch {
                        if (isTracingEnabled) {
                            // check if background processing is switched off,
                            // if it is, show the manual calculation dialog explanation before turning on.
                            if (!backgroundStatus.isIgnoringBatteryOptimizations.first()) {
                                events.postValue(Event.ManualCheckingDialog)
                            }
                            exposureWindowRiskWorkScheduler.setPeriodicRiskCalculation(enabled = true)
                        }
                    }
                }

                override fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit) {
                    events.postValue(
                        Event.TracingConsentDialog { consentGiven ->
                            onConsentResult(consentGiven)
                        }
                    )
                }

                override fun onPermissionRequired(permissionRequest: (Activity) -> Unit) {
                    events.postValue(Event.RequestPermissions(permissionRequest))
                }

                override fun onError(error: Throwable) {
                    Timber.w(error, "Failed to start tracing from settings screen.")
                    ensErrorEvents.postValue(error)
                }
            }
        )

    fun turnTracingOn() {
        try {
            tracingPermissionHelper.startTracing()
        } catch (exception: Exception) {
            exception.report(
                ExceptionCategory.EXPOSURENOTIFICATION,
                TracingSettingsFragment.TAG,
                null
            )
        }
    }

    fun turnTracingOff() {
        launch {
            try {
                if (enfClient.disableTracingIfEnabled()) {
                    exposureWindowRiskWorkScheduler.setPeriodicRiskCalculation(enabled = false)
                }
            } catch (exception: Exception) {
                exception.report(
                    ExceptionCategory.EXPOSURENOTIFICATION,
                    TracingSettingsFragment.TAG,
                    null
                )
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tracingPermissionHelper.handleActivityResult(requestCode, resultCode, data)
    }

    sealed class Event {
        data class RequestPermissions(val permissionRequest: (Activity) -> Unit) : Event()
        data class TracingConsentDialog(val onConsentResult: (Boolean) -> Unit) : Event()
        object ManualCheckingDialog : Event()
    }
}
