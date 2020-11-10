package de.rki.coronawarnapp.ui.submission.warnothers

import androidx.lifecycle.asLiveData
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionTask
import de.rki.coronawarnapp.submission.Symptoms
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
    @Assisted private val symptoms: Symptoms,
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    private val taskController: TaskController,
    interoperabilityRepository: InteroperabilityRepository,
    private val testResultNotificationService: TestResultNotificationService
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

    val requestKeySharing = SingleLiveEvent<Unit>()
    val showEnableTracingEvent = SingleLiveEvent<Unit>()

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
                requestKeySharing.postValue(Unit)
            } else {
                showEnableTracingEvent.postValue(Unit)
            }
        }
    }

    fun onKeysShared(keys: List<TemporaryExposureKey>) {
        if (keys.isNotEmpty()) {
            submitDiagnosisKeys(keys)
        } else {
            submitWithNoDiagnosisKeys()
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSubmissionDone)
        }
        testResultNotificationService.cancelPositiveTestResultNotification()
    }

    private fun submitDiagnosisKeys(keys: List<TemporaryExposureKey>) {
        Timber.d("submitDiagnosisKeys(keys=%s, symptoms=%s)", keys, symptoms)
        val registrationToken =
                LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        val taskRequest = DefaultTaskRequest(
                SubmissionTask::class,
                SubmissionTask.Arguments(registrationToken, keys, symptoms)
        )
        currentSubmissionRequestId = taskRequest.id
        taskController.submit(taskRequest)
    }

    private fun submitWithNoDiagnosisKeys() {
        Timber.d("submitWithNoDiagnosisKeys()")
        SubmissionService.submissionSuccessful()
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionResultPositiveOtherWarningViewModel> {
        fun create(symptoms: Symptoms): SubmissionResultPositiveOtherWarningViewModel
    }
}
