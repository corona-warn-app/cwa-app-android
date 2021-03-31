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
class TraceTimeWarningServer @Inject constructor(
    private val traceTimeWarningApi: Lazy<TraceTimeWarningApiV1>
) {

    private val warningApi: TraceTimeWarningApiV1
        get() = traceTimeWarningApi.get()

    suspend fun getAvailableIds(
        location: LocationCode
    ): TraceTimeWarningApiV1.DiscoveryResult = withContext(Dispatchers.IO) {
        warningApi.getWarningPackageIds(location.identifier).also {
            Timber.d("getAvailableIds(location=%s): %s", location, it)
        }
    }

    suspend fun downloadPackage(
        location: LocationCode,
        hourInterval: HourInterval
    ): TraceTimeWarningDownload = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v("downloadPackage(location=%s, hourInterval=%s)", location, hourInterval)

        val response = warningApi.downloadKeyFileForHour(
            location.identifier,
            hourInterval
        )

        val downloadInfo = TraceTimeWarningDownload(response)

        if (response.isSuccessful) {
            Timber.tag(TAG).v("TraceTimeWarning download available: %s", downloadInfo)

            return@withContext downloadInfo
        } else {
            throw HttpException(response)
        }
    }
}

private val TAG = TraceTimeWarningServer::class.java.simpleName
