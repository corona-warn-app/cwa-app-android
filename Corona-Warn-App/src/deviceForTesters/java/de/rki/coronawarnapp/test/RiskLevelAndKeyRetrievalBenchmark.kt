package de.rki.coronawarnapp.test

import android.content.Context
import android.text.format.Formatter
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.di.AppInjector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID

class RiskLevelAndKeyRetrievalBenchmark(
    private val context: Context,
    private val countries: List<String>
) {

    /**
     * the key cache instance used to store queried dates and hours
     */
    private val keyCache = AppInjector.component.keyCacheRepository

    /**
     * Calls the RetrieveDiagnosisKeysTransaction and RiskLevelTransaction and measures them.
     * Results are displayed using a label
     * @param callCount defines how often the transactions should be called (each call will be
     * measured separately)
     */
    suspend fun start(
        callCount: Int,
        onBenchmarkCompletedListener: OnBenchmarkCompletedListener
    ) {

        var resultInfo = StringBuilder()
            .append(
                "MEASUREMENT Running for Countries:\n " +
                        "${countries.joinToString(", ")}\n\n"
            )
            .append("Result: \n\n")
            .append("#\t Combined \t Download \t Sub \t Risk \t File # \t  F. size\n")

        onBenchmarkCompletedListener(resultInfo.toString())

        repeat(callCount) { index ->

            keyCache.clear()

            var keyRetrievalError = ""
            var keyFileCount: Int = -1
            var keyFileDownloadDuration: Long = -1
            var keyFilesSize: Long = -1
            var apiSubmissionDuration: Long = -1

            try {
                measureDiagnosticKeyRetrieval(
                    label = "#$index",
                    countries = countries,
                    downloadFinished = { duration, keyCount, totalFileSize ->
                        keyFileCount = keyCount
                        keyFileDownloadDuration = duration
                        keyFilesSize = totalFileSize
                    }, apiSubmissionFinished = { duration ->
                        apiSubmissionDuration = duration
                    })
            } catch (e: TransactionException) {
                keyRetrievalError = e.message.toString()
            }

            var calculationDuration: Long = -1
            var calculationError = ""

            measureKeyCalculation("#$index") {
                if (it != null) calculationDuration = it

                // build result entry for current iteration with all gathered data
                resultInfo.append(
                    "${index + 1}. \t ${calculationDuration + keyFileDownloadDuration + apiSubmissionDuration} ms \t " +
                        "$keyFileDownloadDuration ms " + "\t $apiSubmissionDuration ms" +
                        "\t $calculationDuration ms \t $keyFileCount \t " +
                        "${Formatter.formatFileSize(context, keyFilesSize)}\n"
                )

                if (keyRetrievalError.isNotEmpty()) {
                    resultInfo.append("Key Retrieval Error: $keyRetrievalError\n")
                }

                if (calculationError.isNotEmpty()) {
                    resultInfo.append("Calculation Error: $calculationError\n")
                }

                onBenchmarkCompletedListener(resultInfo.toString())
            }
        }
    }

    private suspend fun measureKeyCalculation(label: String, callback: (Long?) -> Unit) {
        val uuid = UUID.randomUUID()
        val t0 = System.currentTimeMillis()
        AppInjector.component.taskController.tasks
            .map {
                it
                    .map { taskInfo -> taskInfo.taskState }
                    .filter { taskState -> taskState.request.id == uuid && taskState.isFinished }
            }
            .collect {
                it.firstOrNull()?.also { state ->
                    Timber.v("MEASURE [Risk Level Calculation] $label finished")
                    callback.invoke(
                        if (state.error != null)
                            null
                        else
                            System.currentTimeMillis() - t0
                    )
                }
            }
        Timber.v("MEASURE [Risk Level Calculation] $label started")
        AppInjector.component.taskController.submit(
            DefaultTaskRequest(
                RiskLevelTask::class,
                object : Task.Arguments {},
                uuid
            )
        )
    }

    private suspend fun measureDiagnosticKeyRetrieval(
        label: String,
        countries: List<String>,
        downloadFinished: (duration: Long, keyCount: Int, fileSize: Long) -> Unit,
        apiSubmissionFinished: (duration: Long) -> Unit
    ) {
        var keyFileDownloadStart: Long = -1
        var apiSubmissionStarted: Long = -1

        try {
            RetrieveDiagnosisKeysTransaction.onKeyFilesDownloadStarted = {
                Timber.v("MEASURE [Diagnostic Key Files] $label started")
                keyFileDownloadStart = System.currentTimeMillis()
            }

            RetrieveDiagnosisKeysTransaction.onKeyFilesDownloadFinished = { count, size ->
                Timber.v("MEASURE [Diagnostic Key Files] $label finished")
                val duration = System.currentTimeMillis() - keyFileDownloadStart
                downloadFinished(duration, count, size)
            }

            RetrieveDiagnosisKeysTransaction.onApiSubmissionStarted = {
                apiSubmissionStarted = System.currentTimeMillis()
            }

            RetrieveDiagnosisKeysTransaction.onApiSubmissionFinished = {
                val duration = System.currentTimeMillis() - apiSubmissionStarted
                apiSubmissionFinished(duration)
            }

            // start diagnostic key transaction
            RetrieveDiagnosisKeysTransaction.start(countries)
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
            throw e
        }
    }
}

typealias OnBenchmarkCompletedListener = (resultInfo: String) -> Unit
