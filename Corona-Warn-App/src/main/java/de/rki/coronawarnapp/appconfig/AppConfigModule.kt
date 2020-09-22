package de.rki.coronawarnapp.appconfig

import android.content.Context
import androidx.annotation.VisibleForTesting
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.joda.time.Duration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppConfigModule {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getConfigCachePath(context: Context): File {
        // We switch from `cacheDir` to `filesDir` so that the system may not clear our fallback.
        val configStore = File(context.filesDir, "appconfig_httpstore")
        val legacyCache = File(context.cacheDir, "http_app-config")
        try {
            if (legacyCache.exists()) {
                val migrated = legacyCache.copyRecursively(configStore) { file, err ->
                    Timber.e(err, "Failed to migrate: %s", file)
                    OnErrorAction.SKIP
                }
                Timber.i("AppConfig cache migrated: %b", migrated)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to migrate legacy AppConfig cache.")
        } finally {
            try {
                legacyCache.deleteRecursively()
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete legacy AppConfig cache.")
            }
        }
        return configStore
    }

    @Singleton
    @Provides
    fun provideAppConfigApi(
        context: Context,
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): AppConfigApiV1 {
        val cacheSize = 1 * 1024 * 1024L // 1MB

        val cache = Cache(getConfigCachePath(context), cacheSize)

        val cachingClient = client.newBuilder().apply {
            cache(cache)
            connectTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            readTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            writeTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
            callTimeout(HTTP_TIMEOUT_APPCONFIG.millis, TimeUnit.MILLISECONDS)
        }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(AppConfigApiV1::class.java)
    }

    companion object {
        private val HTTP_TIMEOUT_APPCONFIG = Duration.standardSeconds(10)
    }
}
