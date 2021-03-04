package de.rki.coronawarnapp.bugreporting.debuglog.upload

import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.LogUploadServer
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadAuthorization
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapshotUploader @Inject constructor(
    private val snapshotter: LogSnapshotter,
    private val uploadServer: LogUploadServer,
    private val authorization: LogUploadAuthorization,
    private val bugReportingSettings: BugReportingSettings
) {

    suspend fun uploadSnapshot(): LogUpload {
        Timber.tag(TAG).v("uploadSnapshot()")

        val authorizedOtp = authorization.getAuthorizedOTP().also {
            Timber.tag(TAG).d("Authorized OTP obtained: %s", it)
        }

        val snapshot = snapshotter.snapshot().also {
            Timber.tag(TAG).d("Snapshot created: %s", it)
        }

        val logUpload = uploadServer.uploadLog(authorizedOtp, snapshot).also {
            Timber.tag(TAG).d("Log uploaded: %s", it)
        }

        snapshot.delete().also {
            Timber.tag(TAG).d("Snapshot was deleted after upload: %b", it)
        }

        bugReportingSettings.uploadHistory.update { oldHistory ->
            val newLogs = oldHistory.logs.toMutableList()
            if (newLogs.size > 10) {
                newLogs.removeFirst().also {
                    Timber.tag(TAG).d("Removed oldest entry from history: %s", it)
                }
            }
            newLogs.add(logUpload)
            oldHistory.copy(logs = newLogs)
        }

        return logUpload
    }

    companion object {
        private const val TAG = "SnapshotUploader"
    }
}
