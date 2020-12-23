package de.rki.coronawarnapp.appconfig.sources.remote

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.download.AppConfigApiV2
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.appconfig.internal.InternalConfigData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.retrofit.etag
import de.rki.coronawarnapp.util.security.VerificationKeys
import okhttp3.CacheControl
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class AppConfigServer @Inject constructor(
    private val api: Lazy<AppConfigApiV2>,
    private val verificationKeys: VerificationKeys,
    private val timeStamper: TimeStamper
) {

    internal suspend fun downloadAppConfig(): InternalConfigData {
        Timber.tag(TAG).d("Fetching app config.")

        val response = api.get().getApplicationConfiguration()
        if (!response.isSuccessful) throw HttpException(response)

        val rawConfig = with(
            requireNotNull(response.body()) { "Response was successful but body was null" }
        ) {
            val fileMap = byteStream().unzip().readIntoMap()

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

        val localTime = timeStamper.nowUTC

        val headers = response.headers()

        // Shouldn't happen, but hey ¯\_(ツ)_/¯
        val etag = headers.etag()
            ?: throw ApplicationConfigurationInvalidException(message = "Server has no ETAG.")

        val serverTime = response.getServerDate() ?: localTime
        val offset = Duration(serverTime, localTime)
        Timber.tag(TAG).v("Time offset was %dms", offset.millis)

        val cacheControl = CacheControl.parse(headers)

        val maxCacheAge = Duration.standardSeconds(cacheControl.maxAgeSeconds.toLong())

        return InternalConfigData(
            rawData = rawConfig,
            etag = etag,
            serverTime = serverTime,
            localOffset = offset,
            cacheValidity = maxCacheAge
        )
    }

    private fun <T> Response<T>.getServerDate(): Instant? = try {
        val rawDate = headers()["Date"] ?: throw IllegalArgumentException(
            "Server date unavailable: ${headers()}"
        )
        Instant.parse(rawDate, DATE_FORMAT)
    } catch (e: Exception) {
        Timber.e("Failed to get server time.")
        null
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
