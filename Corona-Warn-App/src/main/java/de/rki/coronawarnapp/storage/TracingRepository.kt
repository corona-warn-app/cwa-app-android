package de.rki.coronawarnapp.storage

import android.annotation.SuppressLint
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingRiskWorkScheduler
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTask
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTaskProgress
import de.rki.coronawarnapp.risk.EwRiskLevelTask
import de.rki.coronawarnapp.risk.execution.ExposureWindowRiskWorkScheduler
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.tracing.RiskCalculationState
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Tracing Repository provide the state of cu.
 */
@Singleton
class TracingRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    taskController: TaskController,
    enfClient: ENFClient,
    private val exposureWindowRiskWorkScheduler: ExposureWindowRiskWorkScheduler,
    private val presenceTracingRiskWorkScheduler: PresenceTracingRiskWorkScheduler,
) {

    @SuppressLint("BinaryOperationInTimber")
    val riskCalculationState: Flow<RiskCalculationState> = combine(
        taskController.tasks,
        enfClient.isPerformingExposureDetection(),
    ) { taskInfos, isExposureDetecting ->

        val isEWDownloading = taskInfos.isEWDownloadingPackages()
        val isEWCalculatingRisk = taskInfos.isEWCalculatingRisk()

        val isPTDownloading = taskInfos.isPTDownloadingPackages()
        val isPTCalculatingRisk = taskInfos.isPTCalculatingRisk()

        when {
            isEWDownloading || isPTDownloading -> RiskCalculationState.Downloading
            isExposureDetecting || isEWCalculatingRisk || isPTCalculatingRisk -> RiskCalculationState.IsCalculating
            else -> RiskCalculationState.Idle
        }.also {
            Timber.tag(TAG).v(
                "TracingProgress: $it, isExposureDetecting=$isExposureDetecting, " +
                    "isEWDownloading=$isEWDownloading, isEWCalculatingRisk=$isEWCalculatingRisk, " +
                    "isPTDownloading=$isPTDownloading, isPTCalculatingRisk=$isPTCalculatingRisk"
            )
        }
    }

    private suspend fun List<TaskInfo>.isPTDownloadingPackages() = any {
        it.taskState.isActive && it.taskState.request.type == PresenceTracingWarningTask::class &&
            it.progress.firstOrNull() is PresenceTracingWarningTaskProgress.Downloading
    }

    private suspend fun List<TaskInfo>.isPTCalculatingRisk() = any {
        it.taskState.isActive && it.taskState.request.type == PresenceTracingWarningTask::class &&
            it.progress.firstOrNull() is PresenceTracingWarningTaskProgress.Calculating
    }

    private fun List<TaskInfo>.isEWDownloadingPackages() = any {
        it.taskState.isActive && it.taskState.request.type == DownloadDiagnosisKeysTask::class
    }

    private fun List<TaskInfo>.isEWCalculatingRisk() = any {
        it.taskState.isActive && it.taskState.request.type == EwRiskLevelTask::class
    }

    fun runRiskCalculations() = scope.launch {
        Timber.tag(TAG).d("runRiskCalculations()")
        exposureWindowRiskWorkScheduler.runRiskTasksNow(sourceTag = TAG)
        presenceTracingRiskWorkScheduler.runRiskTaskNow(sourceTag = TAG)
    }

    companion object {
        private const val TAG: String = "TracingRepository"
    }
}
