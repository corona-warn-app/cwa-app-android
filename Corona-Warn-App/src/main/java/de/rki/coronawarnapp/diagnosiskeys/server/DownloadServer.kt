package de.rki.coronawarnapp.diagnosiskeys.server

import com.google.protobuf.InvalidProtocolBufferException
import dagger.Lazy
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.VerificationKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadServer @Inject constructor(
    private val downloadAPI: Lazy<DownloadApiV1>,
    private val verificationKeys: VerificationKeys,
    @DownloadHomeCountry private val homeCountry: LocationCode
) {

    private val api: DownloadApiV1
        get() = downloadAPI.get()

    suspend fun downloadAppConfig(): ApplicationConfigurationOuterClass.ApplicationConfiguration =
        withContext(Dispatchers.IO) {
            var exportBinary: ByteArray? = null
            var exportSignature: ByteArray? = null
            api.getApplicationConfiguration(homeCountry.identifier).byteStream()
                .unzip { entry, entryContent ->
                    if (entry.name == EXPORT_BINARY_FILE_NAME) exportBinary =
                        entryContent.copyOf()
                    if (entry.name == EXPORT_SIGNATURE_FILE_NAME) exportSignature =
                        entryContent.copyOf()
                }
            if (exportBinary == null || exportSignature == null) {
                throw ApplicationConfigurationInvalidException()
            }

            if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
                throw ApplicationConfigurationCorruptException()
            }

            try {
                return@withContext ApplicationConfigurationOuterClass.ApplicationConfiguration.parseFrom(
                    exportBinary
                )
            } catch (e: InvalidProtocolBufferException) {
                throw ApplicationConfigurationInvalidException()
            }
        }

    /**
     * Gets the country index which is then filtered by given filter param or if param not set
     * @param wantedCountries (array of country codes) used to filter
     * only wanted countries of the country index (case insensitive)
     */
    suspend fun getCountryIndex(
        wantedCountries: List<String>
    ): List<LocationCode> = withContext(Dispatchers.IO) {
        api
            .getCountryIndex().filter {
                wantedCountries
                    .map { c -> c.toUpperCase(Locale.ROOT) }
                    .contains(it.toUpperCase(Locale.ROOT))
            }
            .map { LocationCode(it) }
    }

    suspend fun getDayIndex(location: LocationCode): List<LocalDate> = withContext(Dispatchers.IO) {
        api
            .getDayIndex(location.identifier)
            .map { dayString ->  // 2020-08-19
                LocalDate.parse(dayString, DAY_FORMATTER)
            }
    }

    suspend fun getHourIndex(location: LocationCode, day: LocalDate): List<LocalTime> =
        withContext(Dispatchers.IO) {
            api
                .getHourIndex(location.identifier, day.toString(DAY_FORMATTER))
                .map { hourString -> LocalTime.parse(hourString, HOUR_FORMATTER) }
        }

    /**
     * Retrieves Key Files from the Server
     * Leave **[hour]** null to download a day package
     */
    suspend fun downloadKeyFile(
        locationCode: LocationCode,
        day: LocalDate,
        hour: LocalTime? = null,
        saveTo: File
    ) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v(
            "Starting download: country=%s, day=%s, hour=%s -> %s.",
            locationCode, day, hour, saveTo
        )

        if (saveTo.exists()) {
            Timber.tag(TAG).w("File existed, overwriting: %s", saveTo)
            saveTo.delete()
        }

        saveTo.outputStream().use {

            val streamingBody = if (hour != null) {
                api.downloadKeyFileForHour(
                    locationCode.identifier,
                    day.toString(DAY_FORMATTER),
                    hour.toString(HOUR_FORMATTER)
                )
            } else {
                api.downloadKeyFileForDay(
                    locationCode.identifier,
                    day.toString(DAY_FORMATTER)
                )
            }
            streamingBody.byteStream().copyTo(it, DEFAULT_BUFFER_SIZE)
        }
        Timber.tag(TAG).v("Key file download successful: %s", saveTo)
    }

    companion object {
        private val TAG = DownloadServer::class.java.simpleName
        private val DAY_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd")
        private val HOUR_FORMATTER = DateTimeFormat.forPattern("HH")

        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"

    }
}
