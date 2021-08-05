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
    private val traceWarningUnencryptedApi: Lazy<TraceWarningUnencryptedApiV1>,
    private val traceWarningEncryptedApi: Lazy<TraceWarningEncryptedApiV2>,
) {

    suspend fun getAvailableIds(
        mode: TraceWarningApi.Mode,
        location: LocationCode
    ): DiscoveryResult = withContext(Dispatchers.IO) {
        warningApi(mode).getWarningPackageIds(location.identifier).also {
            Timber.d("getAvailableIds(mode=%s,location=%s): %s", mode, location, it)
        }
    }

    suspend fun downloadPackage(
        mode: TraceWarningApi.Mode,
        location: LocationCode,
        hourInterval: HourInterval
    ): TraceWarningPackageDownload = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v("downloadPackage(mode=%s,location=%s, hourInterval=%s)", mode, location, hourInterval)

        val response = warningApi(mode).downloadKeyFileForHour(
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

    private fun warningApi(mode: TraceWarningApi.Mode): TraceWarningApi =
        when (mode) {
            TraceWarningApi.Mode.UNENCRYPTED -> traceWarningUnencryptedApi.get()
            TraceWarningApi.Mode.ENCRYPTED -> traceWarningEncryptedApi.get()
        }

    companion object {
        private val TAG = TraceWarningServer::class.java.simpleName
    }
}
