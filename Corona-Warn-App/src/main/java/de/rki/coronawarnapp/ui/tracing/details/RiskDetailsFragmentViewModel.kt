package de.rki.coronawarnapp.ui.tracing.details

import androidx.lifecycle.LiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.tracing.card.TracingCardState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardViewModel
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RiskDetailsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    val tracingViewModel: TracingViewModel,
    val settingsViewModel: SettingsViewModel,
    private val tracingDetailsViewModel: TracingDetailsViewModel,
    private val tracingCardViewModel: TracingCardViewModel
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(
        tracingViewModel,
        settingsViewModel,
        tracingDetailsViewModel,
        tracingCardViewModel
    )
) {

    val tracingDetailsState: LiveData<TracingDetailsState> by lazy {
        tracingDetailsViewModel.state
    }

    val tracingCardState: LiveData<TracingCardState> by lazy {
        tracingCardViewModel.state
    }

    fun refreshData() {
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        TimerHelper.checkManualKeyRetrievalTimer()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
    }

    fun updateRiskDetails() {
        tracingViewModel.refreshDiagnosisKeys()
        settingsViewModel.updateManualKeyRetrievalEnabled(false)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<RiskDetailsFragmentViewModel>
}
