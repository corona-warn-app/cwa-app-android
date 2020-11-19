package de.rki.coronawarnapp.ui.tracing.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.tracing.card.TracingCardState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample

class RiskDetailsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    val settingsViewModel: SettingsViewModel,
    tracingDetailsStateProvider: TracingDetailsStateProvider,
    tracingCardStateProvider: TracingCardStateProvider,
    private val tracingRepository: TracingRepository
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(settingsViewModel)
) {

    val tracingDetailsState: LiveData<TracingDetailsState> = tracingDetailsStateProvider.state
        .sample(150L)
        .asLiveData(dispatcherProvider.Default)

    val tracingCardState: LiveData<TracingCardState> = tracingCardStateProvider.state
        .map { it.copy(showDetails = true) }
        .sample(150L)
        .asLiveData(dispatcherProvider.Default)

    fun refreshData() {
        tracingRepository.refreshRiskLevel()
        tracingRepository.refreshExposureSummary()
        TimerHelper.checkManualKeyRetrievalTimer()
        tracingRepository.refreshActiveTracingDaysInRetentionPeriod()
    }

    fun updateRiskDetails() {
        tracingRepository.refreshDiagnosisKeys()
        settingsViewModel.updateManualKeyRetrievalEnabled(false)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<RiskDetailsFragmentViewModel>
}
