package de.rki.coronawarnapp.presencetracing.warning.download.server

import dagger.Lazy
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.HourInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceWarningServer @Inject constructor(
    private val traceWarningApi: Lazy<TraceWarningApiV1>
) {

    private val warningApi: TraceWarningApiV1
        get() = traceWarningApi.get()

    suspend fun getAvailableIds(
        location: LocationCode
    ): TraceWarningApiV1.DiscoveryResult = withContext(Dispatchers.IO) {
        warningApi.getWarningPackageIds(location.identifier).also {
            Timber.d("getAvailableIds(location=%s): %s", location, it)
        }
    }

    suspend fun downloadPackage(
        location: LocationCode,
        hourInterval: HourInterval
    ): TraceWarningPackageDownload = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v("downloadPackage(location=%s, hourInterval=%s)", location, hourInterval)

        val response = warningApi.downloadKeyFileForHour(
            location.identifier,
            hourInterval
        )

        val downloadInfo = TraceWarningPackageDownload(response)

        if (response.isSuccessful) {
            Timber.tag(TAG).v("TraceTimeWarning download available: %s", downloadInfo)

            return@withContext downloadInfo
        } else {
            throw HttpException(response)
        }
    }

    companion object {
        private val TAG = TraceWarningServer::class.java.simpleName
    }
}
