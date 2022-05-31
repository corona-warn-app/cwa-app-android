package de.rki.coronawarnapp.bugreporting.debuglog.upload.server

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadOtp
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.files.determineMimeType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import javax.inject.Inject

@Reusable
class LogUploadServer @Inject constructor(
    private val uploadApiProvider: Lazy<LogUploadApiV1>,
    private val timeStamper: TimeStamper
) {

    private val uploadApi: LogUploadApiV1
        get() = uploadApiProvider.get()

    suspend fun uploadLog(uploadOtp: LogUploadOtp, snapshot: LogSnapshotter.Snapshot): LogUpload {
        val response = uploadApi.uploadLog(
            otp = uploadOtp.otp,
            logZip = MultipartBody.Part.createFormData(
                name = "file",
                filename = snapshot.path.name,
                body = snapshot.path.asRequestBody(snapshot.path.determineMimeType().toMediaType())
            )
        )
        Timber.tag(TAG).d("Upload response: %s", response)

        return LogUpload(id = response.id, uploadedAt = timeStamper.nowUTC)
    }

    companion object {
        private const val TAG = "LogUploadServer"
    }
}
