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

        val rawConfig = response.body()!!.use { body ->
            var exportBinary: ByteArray? = null
            var exportSignature: ByteArray? = null

            body.byteStream().unzip { entry, entryContent ->
                if (entry.name == EXPORT_BINARY_FILE_NAME) {
                    exportBinary = entryContent.copyOf()
                }
                if (entry.name == EXPORT_SIGNATURE_FILE_NAME) {
                    exportSignature = entryContent.copyOf()
                }
            }
            if (exportBinary == null || exportSignature == null) {
                throw ApplicationConfigurationInvalidException()
            }

            if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
                throw ApplicationConfigurationCorruptException()
            }
            exportBinary!!
        }

        val localTime = timeStamper.nowUTC
        val serverTime = try {
            val rawDate = response.headers()["Date"] ?: throw IllegalArgumentException(
                "Server date unavailable: ${response.headers()}"
            )
            Instant.parse(rawDate, DATE_FORMAT)
        } catch (e: Exception) {
            Timber.e("Failed to get server time.")
            localTime
        }
        return ConfigDownload(
            rawData = rawConfig,
            serverTime = serverTime,
            localOffset = Duration(serverTime, localTime)
        )
    }

    internal fun clearCache() {
        Timber.tag(TAG).v("clearCache()")
        cache.evictAll()
    }

    data class ConfigDownload(
        val rawData: ByteArray,
        val serverTime: Instant,
        val localOffset: Duration
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConfigDownload

            if (!rawData.contentEquals(other.rawData)) return false
            if (serverTime != other.serverTime) return false

            return true
        }

        override fun hashCode(): Int {
            var result = rawData.contentHashCode()
            result = 31 * result + serverTime.hashCode()
            return result
        }
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private val DATE_FORMAT = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
        private val TAG = AppConfigServer::class.java.simpleName
    }
}
