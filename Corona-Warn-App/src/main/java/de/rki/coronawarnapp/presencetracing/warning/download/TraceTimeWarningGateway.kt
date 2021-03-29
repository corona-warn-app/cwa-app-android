package de.rki.coronawarnapp.presencetracing.warning.download

import dagger.Lazy
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceTimeWarningGateway @Inject constructor(
    private val traceTimeWarningApi: Lazy<TraceTimeWarningApiV1>
) {

    private val warningApi: TraceTimeWarningApiV1
        get() = traceTimeWarningApi.get()


    suspend fun getAvailableWarningPackageIds(): Response<WarningPackageIds> = withContext(Dispatchers.IO) {
       warningApi.getWarningPackageIds()
    }

    suspend fun downloadWarningPackageFile(
        warningPackageId: Long,
        saveTo: File
    ): DownloadInfo = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v(
            "Starting download: warningPackageId = %s -> %s.",
            warningPackageId,
            saveTo
        )

        if (saveTo.exists()) {
            Timber.tag(TAG).w("File existed, overwriting: %s", saveTo)
            if (saveTo.delete()) {
                Timber.tag(TAG).e("%s exists, but can't be deleted.", saveTo)
            }
        }

        val response = warningApi.downloadKeyFileForHour(warningPackageId)

        val downloadInfo = DownloadInfo(response.headers())

        if (response.isSuccessful) {
            saveTo.outputStream().use { target ->
                response.body()!!.byteStream().use { source ->
                    source.copyTo(target, DEFAULT_BUFFER_SIZE)
                }
            }

            Timber.tag(TAG).v("TraceTimeWarning file download successful: %s", downloadInfo)

            return@withContext downloadInfo
        } else {
            throw HttpException(response)
        }
    }
}

private val TAG = TraceTimeWarningGateway::class.java.simpleName
