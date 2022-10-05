package de.rki.coronawarnapp.appconfig.sources.remote

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.download.AppConfigApiV2
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.appconfig.internal.InternalConfigData
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.retrofit.etag
import de.rki.coronawarnapp.util.security.SignatureValidation
import kotlinx.coroutines.flow.first
import okhttp3.CacheControl
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@Reusable
class AppConfigServer @Inject constructor(
    private val api: Lazy<AppConfigApiV2>,
    private val signatureValidation: SignatureValidation,
    private val timeStamper: TimeStamper,
    private val testSettings: TestSettings
) {

    // Remove this annotation after Sonarqube update, used variables are indicated as unused
    @Suppress("unused")
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

            val hasValidSignature = signatureValidation.hasValidSignature(
                exportBinary,
                SignatureValidation.parseTEKStyleSignature(exportSignature)
            )
            if (!hasValidSignature) {
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
        val offset = if (CWADebug.isDeviceForTestersBuild && testSettings.fakeCorrectDeviceTime.first()) {
            Timber.tag(TAG).w("Test setting 'fakeCorrectDeviceTime' is active; time offset is now 0")
            Duration.ZERO
        } else {
            Duration.between(serverTime, localTime)
        }
        Timber.tag(TAG).v("Time offset was %dms", offset.toMillis())

        val cacheControl = CacheControl.parse(headers)

        val maxCacheAge = cacheControl.maxAgeSeconds.let {
            if (it == 0) {
                // Server currently returns `Cache-Control	max-age=0, no-cache, no-store` which breaks our caching
                Timber.tag(TAG).w("Server returned max-age=0: %s", cacheControl)
                Duration.ofSeconds(300)
            } else {
                Duration.ofSeconds(it.toLong())
            }
        }

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
        DATE_FORMAT.parse(rawDate, Instant::from)
    } catch (e: Exception) {
        Timber.e(e, "Failed to get server time.")
        null
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private val DATE_FORMAT = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
            .withLocale(Locale.ENGLISH)
        private val TAG = AppConfigServer::class.java.simpleName
    }
}
