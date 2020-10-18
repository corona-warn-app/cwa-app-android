package de.rki.coronawarnapp.ui.tracing.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsState
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsStateProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import timber.log.Timber

class SettingsTracingFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingDetailsStateProvider: TracingDetailsStateProvider,
    tracingStatus: GeneralTracingStatus
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val tracingDetailsState: LiveData<TracingDetailsState> = tracingDetailsStateProvider.state
        .onEach { Timber.v("tracingDetailsState onEach") }
        .asLiveData(dispatcherProvider.Main)

    val tracingSettingsState: LiveData<TracingSettingsState> = tracingStatus.generalStatus
        .map { it.toTracingSettingsState() }
        .onEach { Timber.v("tracingSettingsState onEach") }
        .stateIn(
            scope = viewModelScope + dispatcherProvider.Default,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
        .mapNotNull { it }
        .asLiveData(dispatcherProvider.Main)

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsTracingFragmentViewModel>
}
