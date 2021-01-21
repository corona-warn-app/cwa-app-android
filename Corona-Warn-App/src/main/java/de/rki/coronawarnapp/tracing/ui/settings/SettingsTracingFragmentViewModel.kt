package de.rki.coronawarnapp.tracing.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.ui.details.items.periodlogged.PeriodLoggedBox
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import timber.log.Timber

class SettingsTracingFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingStatus: GeneralTracingStatus,
    tracingRepository: TracingRepository,
    private val backgroundStatus: BackgroundModeStatus,
    tracingPermissionHelperFactory: TracingPermissionHelper.Factory
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val loggingPeriod: LiveData<PeriodLoggedBox.Item> = tracingRepository.activeTracingDaysInRetentionPeriod
        .map { PeriodLoggedBox.Item(activeTracingDaysInRetentionPeriod = it.toInt()) }
        .onEach { Timber.v("logginPeriod onEach") }
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

    private val tracingPermissionHelper =
        tracingPermissionHelperFactory.create(object : TracingPermissionHelper.Callback {
            override fun onUpdateTracingStatus(isTracingEnabled: Boolean) {
                launch {
                    if (isTracingEnabled) {
                        // check if background processing is switched off,
                        // if it is, show the manual calculation dialog explanation before turning on.
                        if (!backgroundStatus.isIgnoringBatteryOptimizations.first()) {
                            events.postValue(Event.ManualCheckingDialog)
                        }
                        BackgroundWorkScheduler.startWorkScheduler()
                    }
                    isTracingSwitchChecked.postValue(isTracingEnabled)
                }
            }

            override fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit) {
                events.postValue(Event.TracingConsentDialog { consentGiven ->
                    if (!consentGiven) isTracingSwitchChecked.postValue(false)
                    onConsentResult(consentGiven)
                })
            }

            override fun onPermissionRequired(permissionRequest: (Activity) -> Unit) {
                events.postValue(Event.RequestPermissions(permissionRequest))
            }

            override fun onError(error: Throwable) {
                Timber.w(error, "Failed to start tracing")
            }
        })

    fun onTracingToggled(isChecked: Boolean) {
        try {
            if (isChecked) {
                tracingPermissionHelper.startTracing()
            } else {
                isTracingSwitchChecked.postValue(false)
                launch {
                    if (InternalExposureNotificationClient.asyncIsEnabled()) {
                        InternalExposureNotificationClient.asyncStop()
                        BackgroundWorkScheduler.stopWorkScheduler()
                    }
                }
            }
        } catch (exception: Exception) {
            exception.report(
                ExceptionCategory.EXPOSURENOTIFICATION,
                SettingsTracingFragment.TAG,
                null
            )
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

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsTracingFragmentViewModel>
}
