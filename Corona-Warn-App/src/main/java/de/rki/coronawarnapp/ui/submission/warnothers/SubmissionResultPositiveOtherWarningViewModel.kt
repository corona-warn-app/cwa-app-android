package de.rki.coronawarnapp.ui.submission.warnothers

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionTask
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID

class SubmissionResultPositiveOtherWarningViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    private val taskController: TaskController,
    interoperabilityRepository: InteroperabilityRepository,
    private val testResultNotificationService: TestResultNotificationService,
    private val tekHistoryUpdater: TEKHistoryUpdater
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private var currentSubmissionRequestId: UUID? = null
    private val currentSubmission = taskController.tasks
        .map { it.find { taskInfo -> taskInfo.taskState.type == SubmissionTask::class }?.taskState }
    private val submissionState = currentSubmission
        .map { taskState ->
            when {
                taskState == null -> ApiRequestState.IDLE
                taskState.isFailed -> ApiRequestState.FAILED.also { updateUI(taskState) }
                taskState.isFinished -> ApiRequestState.SUCCESS.also { updateUI(taskState) }
                else -> ApiRequestState.STARTED
            }
        }
    val submissionError = SingleLiveEvent<Throwable>()

    val uiState = combineTransform(
        submissionState,
        interoperabilityRepository.countryListFlow
    ) { state, countries ->
        WarnOthersState(
            apiRequestState = state,
            countryList = countries
        ).also { emit(it) }
    }.asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val permissionRequestEvent = SingleLiveEvent<(Activity) -> Unit>()
    val showEnableTracingEvent = SingleLiveEvent<Unit>()

    init {
        tekHistoryUpdater.tekUpdateListener = { teks, error ->
            if (teks != null) {
                val taskRequest = DefaultTaskRequest(SubmissionTask::class)
                currentSubmissionRequestId = taskRequest.id
                taskController.submit(taskRequest)
                testResultNotificationService.cancelPositiveTestResultNotification()
            } else {
                Timber.e(error, "Couldn't temporary exposure key history.")
                submissionError.postValue(error)
            }
        }
    }

    private fun updateUI(taskState: TaskState) {
        if (taskState.request.id == currentSubmissionRequestId) {
            currentSubmissionRequestId = null
            when {
                taskState.isFailed ->
                    submissionError.postValue(taskState.error ?: return)
                taskState.isSuccessful ->
                    routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSubmissionDone)
            }
        }
    }

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTestResult)
    }

    fun onWarnOthersPressed() {
        launch {
            if (enfClient.isTracingEnabled.first()) {
                tekHistoryUpdater.updateTEKHistoryOrRequestPermission { permissionRequest ->
                    permissionRequestEvent.postValue(permissionRequest)
                }
            } else {
                showEnableTracingEvent.postValue(Unit)
            }
        }
    }

    fun onHandleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionResultPositiveOtherWarningViewModel> {
        fun create(): SubmissionResultPositiveOtherWarningViewModel
    }
}
