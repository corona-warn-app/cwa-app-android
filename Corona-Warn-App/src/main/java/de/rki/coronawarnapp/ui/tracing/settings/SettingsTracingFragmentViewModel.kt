package de.rki.coronawarnapp.ui.tracing.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsState
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsStateProvider
import de.rki.coronawarnapp.util.BackgroundPrioritization
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import timber.log.Timber

class SettingsTracingFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingDetailsStateProvider: TracingDetailsStateProvider,
    tracingStatus: GeneralTracingStatus,
    private val backgroundPrioritization: BackgroundPrioritization
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val tracingDetailsState: LiveData<TracingDetailsState> = tracingDetailsStateProvider.state
        .onEach { Timber.v("tracingDetailsState onEach") }
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

    fun startStopTracing() {
        // if tracing is enabled when listener is activated it should be disabled
        launch {
            try {
                if (InternalExposureNotificationClient.asyncIsEnabled()) {
                    InternalExposureNotificationClient.asyncStop()
                    BackgroundWorkScheduler.stopWorkScheduler()
                } else {
                    // tracing was already activated
                    if (LocalData.initialTracingActivationTimestamp() != null) {
                        events.postValue(Event.RequestPermissions)
                    } else {
                        // tracing was never activated
                        // ask for consent via dialog for initial tracing activation when tracing was not
                        // activated during onboarding
                        events.postValue(Event.ShowConsentDialog)
                        // check if background processing is switched off,
                        // if it is, show the manual calculation dialog explanation before turning on.
                        if (!backgroundPrioritization.isBackgroundActivityPrioritized) {
                            events.postValue(Event.ManualCheckingDialog)
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
    }

    sealed class Event {
        object RequestPermissions : Event()
        object ShowConsentDialog : Event()
        object ManualCheckingDialog : Event()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsTracingFragmentViewModel>
}
