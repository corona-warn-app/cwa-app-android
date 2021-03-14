package de.rki.coronawarnapp.bugreporting.processor

import android.content.Context
import android.os.Build
import android.util.Log
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.event.DefaultBugEvent
import de.rki.coronawarnapp.bugreporting.loghistory.RollingLogHistory
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.tryHumanReadableError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBugProcessor @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper,
    private val rollingLogHistory: RollingLogHistory
) : BugProcessor {

    override suspend fun processor(throwable: Throwable, tag: String?, info: String?): BugEvent {
        val formattedError = throwable.tryHumanReadableError(context)

        val crashedAt = timeStamper.nowUTC
        val exceptionClass = throwable::class.java.simpleName
        val stacktrace = Log.getStackTraceString(throwable)
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})"
        val appVersionName = BuildConfig.VERSION_NAME
        val appVersionCode = BuildConfig.VERSION_CODE
        val apiLevel = Build.VERSION.SDK_INT
        val androidVersion = Build.VERSION.RELEASE
        val shortID = BuildConfig.GIT_COMMIT_SHORT_HASH
        val logHistory = rollingLogHistory.getLoglines(50)

        return DefaultBugEvent(
            createdAt = crashedAt,
            tag = tag,
            info = info,
            exceptionClass = exceptionClass,
            exceptionMessage = formattedError.description,
            stackTrace = stacktrace,
            deviceInfo = deviceInfo,
            appVersionName = appVersionName,
            appVersionCode = appVersionCode.toLong(),
            apiLevel = apiLevel,
            androidVersion = androidVersion,
            shortCommitHash = shortID,
            logHistory = logHistory
        )
    }
}
