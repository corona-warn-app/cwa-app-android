package de.rki.coronawarnapp.appconfig.download

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.environment.download.DownloadCDNHomeCountry
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.VerificationKeys
import okhttp3.Cache
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import retrofit2.HttpException
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class AppConfigServer @Inject constructor(
    private val api: Lazy<AppConfigApiV1>,
    private val verificationKeys: VerificationKeys,
    private val timeStamper: TimeStamper,
    @DownloadCDNHomeCountry private val homeCountry: LocationCode,
    @AppConfigHttpCache private val cache: Cache
) {

    internal suspend fun downloadAppConfig(): ConfigDownload {
        Timber.tag(TAG).d("Fetching app config.")

        val response = api.get().getApplicationConfiguration(homeCountry.identifier)
        if (!response.isSuccessful) throw HttpException(response)

        val cacheResponse = response.raw().cacheResponse

        // If this is a cached response, we need the original timestamp to calculate the time offset
        val localTime = cacheResponse?.sentRequestAtMillis?.let {
            Instant.ofEpochMilli(it)
        } ?: timeStamper.nowUTC

        val rawConfig = with(
            requireNotNull(response.body()) { "Response was successful but body was null" }
        ) {
            val fileMap = byteStream().unzip()
                .fold(emptyMap()) { last: Map<String, ByteArray>, (entry, stream) ->
                    last.plus(entry.name to stream.readBytes())
                }

            val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
            val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

            if (exportBinary == null || exportSignature == null) {
                throw ApplicationConfigurationInvalidException(message = "Unknown files: ${fileMap.keys}")
            }

            if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
                throw ApplicationConfigurationCorruptException()
            }

            exportBinary
        }

        val serverTime = try {
            val rawDate = response.headers()["Date"] ?: throw IllegalArgumentException(
                "Server date unavailable: ${response.headers()}"
            )
            Instant.parse(rawDate, DATE_FORMAT)
        } catch (e: Exception) {
            Timber.e("Failed to get server time.")
            localTime
        }
        val offset = Duration(serverTime, localTime)
        Timber.tag(TAG).v("Time offset was %dms", offset.millis)
        return ConfigDownload(
            rawData = rawConfig,
            serverTime = serverTime,
            localOffset = offset
        )
    }

    internal fun clearCache() {
        Timber.tag(TAG).v("clearCache()")
        cache.evictAll()
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private val DATE_FORMAT = DateTimeFormat
            .forPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
            .withLocale(Locale.ROOT)
        private val TAG = AppConfigServer::class.java.simpleName
    }
}
