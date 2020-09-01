package de.rki.coronawarnapp

import android.content.Context
import android.text.format.Formatter
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import timber.log.Timber
import kotlin.system.measureTimeMillis

object RiskLevelAndKeyRetrievalBenchmark {

    /**
     * Calls the RetrieveDiagnosisKeysTransaction and RiskLevelTransaction and measures them.
     * Results are displayed using a label
     * @param callCount defines how often the transactions should be called (each call will be
     * measured separately)
     */
    suspend fun start(
        context: Context?,
        countries: List<String>,
        callCount: Int,
        callback: (resultInfo: String) -> Unit
    ) {

        var resultInfo = StringBuilder()
            .append(
                "MEASUREMENT Running for Countries:\n " +
                        "${countries?.joinToString(", ")}\n\n"
            )
            .append("Result: \n\n")
            .append("#\t Combined \t Download \t Key Calc \t File # \t Files size\n")

        callback(resultInfo.toString())

        repeat(callCount) { index ->
            var keyRetrievalError = ""
            var keyFileCount: Int = -1
            var keyFileDownloadDuration: Long = -1
            var keyFilesSize: Long = -1

            try {
                measureDiagnosticKeyRetrieval(
                    "#$index",
                    countries
                ) { duration, keyCount, totalFileSize ->
                    keyFileCount = keyCount
                    keyFileDownloadDuration = duration
                    keyFilesSize = totalFileSize
                }
            } catch (e: TransactionException) {
                keyRetrievalError = e.message.toString()
            }

            var calculationDuration: Long = -1
            var calculationError = ""

            try {
                measureKeyCalculation("#$index") {
                    calculationDuration = it
                }
            } catch (e: TransactionException) {
                calculationError = e.message.toString()
            }

            // build result entry for current iteration with all gathered data
            resultInfo.append(
                "${index + 1}. \t ${calculationDuration + keyFileDownloadDuration} ms \t\t " +
                        "$keyFileDownloadDuration ms " +
                        "\t\t $calculationDuration ms \t\t $keyFileCount \t\t " +
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

    private suspend fun measureKeyCalculation(label: String, finished: (duration: Long) -> Unit) {
        try {
            Timber.v("MEASURE [Risk Level Calculation] $label started")
            // start risk level calculation and get duration
            measureTimeMillis {
                RiskLevelTransaction.start()
            }.also {
                Timber.v("MEASURE [Risk Level Calculation] $label finished")
                finished(it)
            }
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
            throw e
        }
    }

    private suspend fun measureDiagnosticKeyRetrieval(
        label: String,
        countries: List<String>,
        finished: (duration: Long, keyCount: Int, fileSize: Long) -> Unit
    ) {
        var keyFileDownloadStart: Long = -1

        try {
            RetrieveDiagnosisKeysTransaction.onKeyFilesStarted = {
                Timber.v("MEASURE [Diagnostic Key Files] $label started")
                keyFileDownloadStart = System.currentTimeMillis()
            }

            RetrieveDiagnosisKeysTransaction.onKeyFilesFinished = { count, size ->
                Timber.v("MEASURE [Diagnostic Key Files] $label finished")
                val duration = System.currentTimeMillis() - keyFileDownloadStart
                finished(duration, count, size)
            }
            // start diagnostic key transaction
            RetrieveDiagnosisKeysTransaction.start(countries)
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
            throw e
        }
    }
}
