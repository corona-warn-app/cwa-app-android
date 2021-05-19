package de.rki.coronawarnapp.bugreporting.debuglog.internal

import android.annotation.SuppressLint
import android.util.Log
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import javax.inject.Inject

@SuppressLint("LogNotTimber")
class DebugLogStorageCheck @Inject constructor(
    private val targetPath: File,
    private val lowStorageLimit: Long = 200 * 1000 * 1024L, // 200MB default
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val logWriter: LogWriter
) {
    val isLowStorage = MutableStateFlow(false)
    private var lastCheckAt: Long = 0

    private val availableSpace: Long
        @SuppressLint("UsableSpace")
        get() {
            var eval: File = targetPath
            var parent: File? = eval.parentFile
            while (!eval.exists() && parent != null) {
                eval = parent
                parent = eval.parentFile
            }
            return eval.usableSpace
        }

    suspend fun isLowStorage(forceCheck: Boolean = false): Boolean {
        val now = timeProvider()
        if (!forceCheck && now - lastCheckAt < 5_000) return isLowStorage.value

        val currentSpace = try {
            availableSpace
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call isLowStorage()", e)
            logWriter.write(createStorageCheckErrorLine(e))
            return true
        }

        val isNowLow = currentSpace < lowStorageLimit
        lastCheckAt = now

        when {
            isNowLow && !isLowStorage.value -> {
                isLowStorage.value = true
                logWriter.write(createLowStorageLogLine())
            }
            !isNowLow -> {
                isLowStorage.value = false
            }
        }

        if (isNowLow) {
            Log.w(TAG, "Not enough storage to write debug log (${currentSpace}B free)")
        }

        return isNowLow
    }

    companion object {
        private const val TAG = DebugLogger.TAG
        private val createStorageCheckErrorLine: (Throwable) -> LogLine = {
            LogLine(
                timestamp = System.currentTimeMillis(),
                priority = Log.ERROR,
                tag = TAG,
                message = "Low storage check failed.",
                throwable = it
            )
        }
        private val createLowStorageLogLine: () -> LogLine = {
            LogLine(
                timestamp = System.currentTimeMillis(),
                priority = Log.WARN,
                tag = TAG,
                message = "Low storage, debug logger halted.",
                throwable = null
            )
        }
    }
}
