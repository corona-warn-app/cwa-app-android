package de.rki.coronawarnapp.test.api.ui

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.GoogleApiAvailability
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.EnvironmentSetup.Type.Companion.toEnvironmentType
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.test.api.ui.EnvironmentState.Companion.toEnvironmentState
import de.rki.coronawarnapp.test.api.ui.LoggerState.Companion.toLoggerState
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.smartLiveData
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TestForApiFragmentViewModel @AssistedInject constructor(
    @AppContext private val context: Context,
    private val envSetup: EnvironmentSetup,
    private val testSettings: TestSettings
) : CWAViewModel() {

    val debugOptionsState by smartLiveData {
        DebugOptionsState(
            areNotificationsEnabled = LocalData.backgroundNotification(),
            isHourlyTestingMode = testSettings.isHourKeyPkgMode
        )
    }

    fun setHourlyKeyPkgMode(enabled: Boolean) {
        debugOptionsState.update {
            testSettings.isHourKeyPkgMode = enabled
            it.copy(isHourlyTestingMode = enabled)
        }
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
        AppInjector.component.taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
    }

    val logShareEvent = SingleLiveEvent<File?>()

    fun shareLogFile() {
        CWADebug.fileLogger?.let {
            viewModelScope.launch(context = Dispatchers.Default) {
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

    val gmsState by smartLiveData {
        GoogleServicesState(
            version = PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(
                    GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                    0
                )
            )
        )
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestForApiFragmentViewModel>
}
