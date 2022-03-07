package de.rki.coronawarnapp.risk.execution

import android.annotation.SuppressLint
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.diagnosiskeys.execution.DiagnosisKeyRetrievalWorkBuilder
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.await
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ExposureWindowRiskWorkScheduler @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val taskController: TaskController,
    private val diagnosisWorkBuilder: DiagnosisKeyRetrievalWorkBuilder,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val onboardingSettings: OnboardingSettings,
    private val enfClient: ENFClient
) : RiskWorkScheduler(
    workManager = workManager,
    logTag = TAG,
) {

    @SuppressLint("BinaryOperationInTimber")
    fun setup() {
        Timber.tag(TAG).i("setup() ExposureWindowRiskWorkScheduler")
        combine(
            backgroundModeStatus.isAutoModeEnabled,
            onboardingSettings.isOnboardedFlow,
            enfClient.isTracingEnabled
        ) { isAutoMode, isOnboarded, isTracing ->
            Timber.tag(TAG).d(
                "isAutoMode=$isAutoMode, " +
                    "isOnBoarded=$isOnboarded, " +
                    "isTracing=$isTracing"
            )
            isAutoMode && isOnboarded && isTracing
        }.onEach { runPeriodicWorker ->
            Timber.tag(TAG).v("runPeriodicWorker=$runPeriodicWorker")
            setPeriodicRiskCalculation(enabled = runPeriodicWorker)
        }.launchIn(appScope)
    }

    suspend fun runRiskTasksNow(sourceTag: String) = appScope.launch {
        taskController.submitBlocking(
            DefaultTaskRequest(
                DownloadDiagnosisKeysTask::class,
                DownloadDiagnosisKeysTask.Arguments(),
                originTag = "ExposureWindowRiskWorkScheduler-$sourceTag"
            )
        )
        taskController.submit(
            DefaultTaskRequest(
                RiskLevelTask::class,
                originTag = "ExposureWindowRiskWorkScheduler-$sourceTag"
            )
        )
    }

    override suspend fun isScheduled(): Boolean = workManager
        .getWorkInfosForUniqueWork(WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD)
        .await()
        .any { it.isScheduled }

    override fun setPeriodicRiskCalculation(enabled: Boolean) {
        Timber.tag(TAG).i("setPeriodicRiskCalculation(enabled=$enabled)")

        if (enabled) {
            val diagnosisRequest = diagnosisWorkBuilder.createPeriodicWorkRequest()
            queueWorker(WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD, diagnosisRequest)
        } else {
            cancelWorker(WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD)
        }
    }

    companion object {
        private const val WORKER_ID_DIAGNOSIS_KEY_DOWNLOAD = "DiagnosisKeyRetrievalWorker"
        private const val TAG = "EWRiskWorkScheduler"
    }
}
