package de.rki.coronawarnapp

import android.content.Context
import android.text.format.Formatter
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.di.AppInjector
import timber.log.Timber
import kotlin.system.measureTimeMillis

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
        callback: (resultInfo: String) -> Unit
    ) {

        var resultInfo = StringBuilder()
            .append(
                "MEASUREMENT Running for Countries:\n " +
                        "${countries.joinToString(", ")}\n\n"
            )
            .append("Result: \n\n")
            .append("#\t Combined \t Download \t Sub \t Risk \t File # \t  F. size\n")

        callback(resultInfo.toString())

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

            try {
                calculationDuration = measureKeyCalculation("#$index")
            } catch (e: TransactionException) {
                calculationError = e.message.toString()
            }

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

            callback(resultInfo.toString())
        }
    }

    private suspend fun measureKeyCalculation(label: String): Long {
        try {
            Timber.v("MEASURE [Risk Level Calculation] $label started")
            // start risk level calculation and get duration
            return measureTimeMillis {
                RiskLevelTransaction.start()
            }.also {
                Timber.v("MEASURE [Risk Level Calculation] $label finished")
            }
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
            throw e
        }
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
