package de.rki.coronawarnapp.ccl.configuration

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.ccl.configuration.server.CclConfigurationApiV1
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
object CclConfigurationModule {

    @Singleton
    @CclConfiguration
    @Provides
    // Holds the ccl and ccl http cache. Gets cleared in CclConfigurationRepository
    fun provideCclDir(@AppContext context: Context): File = File(context.filesDir, CCL_DIR)

    @Singleton
    @Provides
    fun provideApi(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @CclConfiguration cclDir: File
    ): CclConfigurationApiV1 {
        val cacheDir = File(cclDir, CCL_CACHE_DIR)
        val cache = Cache(cacheDir, CACHE_SIZE)

        val cclClient = client.newBuilder()
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .client(cclClient)
            .baseUrl(url)
            .build()
            .create(CclConfigurationApiV1::class.java)
    }
}

private const val CCL_DIR = "ccl"
private const val CCL_CACHE_DIR = "ccl_config_http_cache"
private const val CACHE_SIZE = 50 * 1024 * 1024L // 50MB

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CclConfiguration
