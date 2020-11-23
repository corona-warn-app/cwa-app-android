package de.rki.coronawarnapp.test.debugoptions.ui

import android.content.Context
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.test.api.ui.EnvironmentState.Companion.toEnvironmentState
import de.rki.coronawarnapp.test.api.ui.LoggerState.Companion.toLoggerState
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.smartLiveData
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import java.io.File

class DebugOptionsFragmentViewModel @AssistedInject constructor(
    @AppContext private val context: Context,
    private val envSetup: EnvironmentSetup,
    private val testSettings: TestSettings,
    private val taskController: TaskController,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val debugOptionsState by smartLiveData {
        DebugOptionsState(
            areNotificationsEnabled = LocalData.backgroundNotification()
        )
    }

    val environmentState by smartLiveData {
        envSetup.toEnvironmentState()
    }
    val environmentChangeEvent = SingleLiveEvent<EnvironmentSetup.Type>()

    fun selectEnvironmentTytpe(type: String) {
        environmentState.update {
            envSetup.currentEnvironment = type.toEnvironmentType()
            environmentChangeEvent.postValue(envSetup.currentEnvironment)
            envSetup.toEnvironmentState()
        }
    }

    val backgroundNotificationsToggleEvent = SingleLiveEvent<Boolean>()

    fun setBackgroundNotifications(enabled: Boolean) {
        debugOptionsState.update {
            LocalData.backgroundNotification(enabled)
            it.copy(areNotificationsEnabled = enabled)
        }
        backgroundNotificationsToggleEvent.postValue(enabled)
    }

    val loggerState by smartLiveData {
        CWADebug.toLoggerState()
    }

    fun setLoggerEnabled(enable: Boolean) {
        CWADebug.fileLogger?.let {
            if (enable) it.start() else it.stop()
        }
        loggerState.update { CWADebug.toLoggerState() }
    }

    fun calculateRiskLevelClicked() {
        taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
    }

    val logShareEvent = SingleLiveEvent<File?>()

    fun shareLogFile() {
        CWADebug.fileLogger?.let {
            launch {
                if (!it.logFile.exists()) return@launch

                val externalPath = File(
                    context.getExternalFilesDir(null),
                    "LogFile-${System.currentTimeMillis()}.log"
                )

                it.logFile.copyTo(externalPath)

                logShareEvent.postValue(externalPath)
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<DebugOptionsFragmentViewModel>
}
