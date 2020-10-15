package de.rki.coronawarnapp.ui.tracing.settings

import androidx.lifecycle.LiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsState
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SettingsTracingFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val tracingDetailsViewModel: TracingDetailsViewModel
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val tracingDetailsState: LiveData<TracingDetailsState> by lazy {
        tracingDetailsViewModel.state
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsTracingFragmentViewModel>
}
