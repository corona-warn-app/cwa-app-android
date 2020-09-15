package de.rki.coronawarnapp.diagnosiskeys.server

import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosisKeyServer @Inject constructor(
    private val diagnosisKeyAPI: Lazy<DiagnosisKeyApiV1>,
    @DownloadHomeCountry private val homeCountry: LocationCode
) {

    private val keyApi: DiagnosisKeyApiV1
        get() = diagnosisKeyAPI.get()

    suspend fun getCountryIndex(): List<LocationCode> = withContext(Dispatchers.IO) {
        keyApi
            .getCountryIndex()
            .map { LocationCode(it) }
    }

    suspend fun getDayIndex(location: LocationCode): List<LocalDate> = withContext(Dispatchers.IO) {
        keyApi
            .getDayIndex(location.identifier)
            .map { dayString ->
                // 2020-08-19
                LocalDate.parse(dayString, DAY_FORMATTER)
            }
    }

    suspend fun getHourIndex(location: LocationCode, day: LocalDate): List<LocalTime> =
        withContext(Dispatchers.IO) {
            keyApi
                .getHourIndex(location.identifier, day.toString(DAY_FORMATTER))
                .map { hourString -> LocalTime.parse(hourString, HOUR_FORMATTER) }
        }

    interface HeaderHook {
        suspend fun validate(headers: Headers): Boolean = true
    }

    /**
     * Retrieves Key Files from the Server
     * Leave **[hour]** null to download a day package
     */
    suspend fun downloadKeyFile(
        locationCode: LocationCode,
        day: LocalDate,
        hour: LocalTime? = null,
        saveTo: File,
        headerHook: HeaderHook = object : HeaderHook {}
    ) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v(
            "Starting download: country=%s, day=%s, hour=%s -> %s.",
            locationCode, day, hour, saveTo
        )

        if (saveTo.exists()) {
            Timber.tag(TAG).w("File existed, overwriting: %s", saveTo)
            if (saveTo.delete()) {
                Timber.tag(TAG).e("%s exists, but can't be deleted.", saveTo)
            }
        }

        val response = if (hour != null) {
            keyApi.downloadKeyFileForHour(
                locationCode.identifier,
                day.toString(DAY_FORMATTER),
                hour.toString(HOUR_FORMATTER)
            )
        } else {
            keyApi.downloadKeyFileForDay(
                locationCode.identifier,
                day.toString(DAY_FORMATTER)
            )
        }

        if (!headerHook.validate(response.headers())) {
            Timber.tag(TAG).d("validateHeaders() told us to abort.")
            return@withContext
        }
        if (response.isSuccessful) {
            saveTo.outputStream().use { target ->
                response.body()!!.byteStream().use { source ->
                    source.copyTo(target, DEFAULT_BUFFER_SIZE)
                }
            }
            Timber.tag(TAG).v("Key file download successful: %s", saveTo)
        } else {
            throw HttpException(response)
        }
    }

    companion object {
        private val TAG = DiagnosisKeyServer::class.java.simpleName
        private val DAY_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd")
        private val HOUR_FORMATTER = DateTimeFormat.forPattern("HH")
    }
}
