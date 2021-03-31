package de.rki.coronawarnapp.test.eventregistration.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.warning.download.TraceTimeWarningPackageSyncTool
import de.rki.coronawarnapp.presencetracing.warning.worker.PresenceTracingWarningTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val syncTool: TraceTimeWarningPackageSyncTool,
    private val taskController: TaskController
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    fun runWarningPackageTask() {
        launch {
            Timber.d("runWarningPackageTask()")
            taskController.submit(
                DefaultTaskRequest(
                    PresenceTracingWarningTask::class, originTag = "EventRegistrationTestFragmentViewModel"
                )
            )
        }
    }

    fun downloadWarningPackages() {
        launch {
            Timber.d("downloadWarningPackages()")
            syncTool.syncPackages()
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}
