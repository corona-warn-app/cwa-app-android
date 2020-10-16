package de.rki.coronawarnapp.ui.tracing.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsState
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class SettingsTracingFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val tracingDetailsViewModel: TracingDetailsViewModel,
    private val tracingStatus: GeneralTracingStatus
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val tracingDetailsState: LiveData<TracingDetailsState> by lazy {
        tracingDetailsViewModel.state
    }

    val tracingSettingsState: LiveData<TracingSettingsState> by lazy {
        tracingStatus.generalStatus
            .map { it.toTracingSettingsState() }
            .asLiveData(dispatcherProvider.Default)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsTracingFragmentViewModel>
}
