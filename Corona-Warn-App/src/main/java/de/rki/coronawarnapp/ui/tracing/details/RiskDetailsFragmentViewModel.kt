package de.rki.coronawarnapp.ui.tracing.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.tracing.card.TracingCardState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardViewModel
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class RiskDetailsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    val settingsViewModel: SettingsViewModel,
    private val tracingDetailsViewModel: TracingDetailsViewModel,
    private val tracingCardViewModel: TracingCardViewModel
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(
        settingsViewModel,
        tracingDetailsViewModel,
        tracingCardViewModel
    )
) {

    val tracingDetailsState: LiveData<TracingDetailsState> by lazy {
        tracingDetailsViewModel.state
    }

    val tracingCardState: LiveData<TracingCardState> by lazy {
        tracingCardViewModel.state.map { it.copy(showDetails = true) }
    }

    fun refreshData() {
        launch { TracingRepository.refreshRiskLevel() }
        launch { TracingRepository.refreshExposureSummary() }
        TracingRepository.refreshLastTimeDiagnosisKeysFetchedDate()
        TimerHelper.checkManualKeyRetrievalTimer()
        launch { TracingRepository.refreshActiveTracingDaysInRetentionPeriod() }
    }

    fun updateRiskDetails() {
        launch { TracingRepository.refreshDiagnosisKeys() }
        settingsViewModel.updateManualKeyRetrievalEnabled(false)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<RiskDetailsFragmentViewModel>
}
