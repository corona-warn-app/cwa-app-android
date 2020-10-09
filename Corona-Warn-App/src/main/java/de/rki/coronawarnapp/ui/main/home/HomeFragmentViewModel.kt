package de.rki.coronawarnapp.ui.main.home

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskData
import de.rki.coronawarnapp.task.example.ExampleArguments
import de.rki.coronawarnapp.task.example.ExampleTaskRequest
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowErrorResetDialog
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowInteropDeltaOnboarding
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowTracingExplanation
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @AppContext private val context: Context,
    private val errorResetTool: EncryptionErrorResetTool,
    val tracingViewModel: TracingViewModel,
    val settingsViewModel: SettingsViewModel,
    val submissionViewModel: SubmissionViewModel,
    private val taskController: TaskController
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(tracingViewModel, settingsViewModel, submissionViewModel)
) {

    val events = SingleLiveEvent<HomeFragmentEvents>()

    init {
        if (!LocalData.isInteroperabilityShownAtLeastOnce) {
            events.postValue(ShowInteropDeltaOnboarding)
        } else {
            launch {
                if (!LocalData.tracingExplanationDialogWasShown()) {
                    events.postValue(
                        ShowTracingExplanation(TimeVariables.getActiveTracingDaysInRetentionPeriod())
                    )
                }
            }
            launch {
                if (errorResetTool.isResetNoticeToBeShown) {
                    events.postValue(ShowErrorResetDialog)
                }
            }
        }
        taskController.submitTask(
            ExampleTaskRequest(
                arguments = ExampleArguments(arg = "1")
            )
        )
        taskController.submitTask(
            ExampleTaskRequest(
                arguments = ExampleArguments(arg = "2")
            )
        )
        taskController.submitTask(
            ExampleTaskRequest(
                arguments = ExampleArguments(arg = "3")
            )
        )
        taskController.submitTask(
            ExampleTaskRequest(
                arguments = ExampleArguments(arg = "4")
            )
        )

        viewModelScope.launch {
            taskController.tasks
                .flatMapMerge { it.entries.asFlow() }
                .filter { it.key.state == TaskData.State.RUNNING }
                .flatMapMerge { (key, value) ->
                    value.map {
                        key to it
                    }
                }
                .collect {
                    Timber.d("New state: %s", it.second.primaryMessage.get(context))
                }
        }
    }

    fun errorResetDialogDismissed() {
        errorResetTool.isResetNoticeToBeShown = false
    }

    fun refreshRequiredData() {
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        tracingViewModel.refreshIsTracingEnabled()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
        TimerHelper.checkManualKeyRetrievalTimer()
        submissionViewModel.refreshDeviceUIState()
        tracingViewModel.refreshLastSuccessfullyCalculatedScore()
    }

    fun tracingExplanationWasShown() {
        LocalData.tracingExplanationDialogWasShown(true)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
