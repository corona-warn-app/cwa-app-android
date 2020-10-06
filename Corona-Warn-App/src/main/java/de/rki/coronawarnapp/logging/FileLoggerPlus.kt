package de.rki.coronawarnapp.logging

import android.content.Context
import android.text.TextUtils
import android.util.Log
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FileLoggerPlus(private val ctx: Context) : Timber.DebugTree() {

    private val logFile: File by lazy {
        val file = File(ctx.filesDir, "log.txt")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return@lazy file
    }

    private val logBuffer: PublishSubject<String> by lazy {
        val buffer = PublishSubject.create<String>()

        buffer
            .subscribeOn(Schedulers.io())
            .buffer(
                MINUTES_UNTIL_BUFFER_FLUSH,
                TimeUnit.MINUTES,
                Schedulers.io(),
                LOG_LINES_UNTIL_BUFFER_FLUSH
            )
            .subscribeBy(
                onNext = { writeToFile(it) },
                onError = { Timber.e(it) }
            )

        return@lazy buffer
    }

    private val sdf: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        logBuffer.onNext("${sdf.format(Date())}  ${priorityToString(priority)}/$tag: $message")
        t?.let {
            // Log stacktrace of error
            logBuffer.onNext(Log.getStackTraceString(it))
        }
    }

    private fun writeToFile(logLines: List<String>) {
        try {
            FileOutputStream(logFile, true)
                .bufferedWriter()
                .use { writer ->
                    writer.appendln(TextUtils.join(System.lineSeparator(), logLines))
                }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun priorityToString(priority: Int): String = when (priority) {
        Log.ERROR -> "E"
        Log.WARN -> "W"
        Log.INFO -> "I"
        Log.DEBUG -> "D"
        Log.VERBOSE -> "V"
        else -> priority.toString()
    }

    companion object {
        private const val MINUTES_UNTIL_BUFFER_FLUSH = 5L
        private const val LOG_LINES_UNTIL_BUFFER_FLUSH = 20
    }
}
