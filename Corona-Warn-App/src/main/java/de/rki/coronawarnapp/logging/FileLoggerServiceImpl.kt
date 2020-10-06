package de.rki.coronawarnapp.logging

import android.content.Context
import android.text.TextUtils
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.concurrent.TimeUnit

class FileLoggerServiceImpl private constructor(private val logFile: File) : IFileLoggerService {

    private val logBuffer: PublishSubject<LogElement> by lazy {
        val buffer = PublishSubject.create<LogElement>()

        buffer
            .subscribeOn(Schedulers.io())
            .buffer(
                SECONDS_UNTIL_BUFFER_FLUSH,
                TimeUnit.SECONDS,
                Schedulers.io()
            )
            .filter { it.isNotEmpty() }
            .map { logElementListToStringList(it) }
            .subscribeBy(
                onNext = { writeToFile(it) },
                onError = { Timber.e(it) }
            )

        return@lazy buffer
    }

    private fun logElementListToStringList(logElements: List<LogElement>): List<String> {
        return logElements.map { LogElementHelper.logElementToString(it) }
    }

    override fun log(logElement: LogElement) {
        logBuffer.onNext(logElement)
    }

    override fun getFile(): File {
        return logFile
    }

    override fun reset() {
        if (logFile.exists()) {
            logFile.delete()
        }
        logFile.createNewFile()
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

    companion object {
        private const val SECONDS_UNTIL_BUFFER_FLUSH = 1L

        private var instance: FileLoggerServiceImpl? = null

        fun getInstance(ctx: Context): FileLoggerServiceImpl {
            return instance ?: synchronized(this) {
                instance ?: getFileLoggerServiceImpl(ctx).also { instance = it }
            }
        }

        private fun getFileLoggerServiceImpl(ctx: Context): FileLoggerServiceImpl {
            val file = File(ctx.filesDir, "log.txt")
            file.createNewFile()
            return FileLoggerServiceImpl(file)
        }
    }
}
