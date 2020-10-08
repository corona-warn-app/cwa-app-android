package de.rki.coronawarnapp.ui.main.home

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.task.TaskType
import de.rki.coronawarnapp.task.example.ExampleArguments
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowErrorResetDialog
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowInteropDeltaOnboarding
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowTracingExplanation
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
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
            TaskRequest(
                type = TaskType.EXAMPLE,
                arguments = ExampleArguments(arg = "1")
            )
        )
        taskController.submitTask(
            TaskRequest(
                type = TaskType.EXAMPLE,
                arguments = ExampleArguments(arg = "2")
            )
        )
        taskController.submitTask(
            TaskRequest(
                type = TaskType.EXAMPLE,
                arguments = ExampleArguments(arg = "3")
            )
        )
        taskController.submitTask(
            TaskRequest(
                type = TaskType.EXAMPLE,
                arguments = ExampleArguments(arg = "4")
            )
        )
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
