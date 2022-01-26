package de.rki.coronawarnapp.ccl.configuration

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.ccl.configuration.server.CCLConfigurationApiV1
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
object CCLConfigurationModule {

    @Singleton
    @CCLConfiguration
    @Provides
    // Holds the ccl and ccl http cache. Gets cleared in CCLConfigurationRepository
    fun provideCCLDir(@AppContext context: Context): File = File(context.filesDir, CCL_DIR)

    @Singleton
    @Provides
    fun provideApi(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @CCLConfiguration cclDir: File
    ): CCLConfigurationApiV1 {
        val cacheDir = File(cclDir, CCL_CACHE_DIR)
        val cache = Cache(cacheDir, CACHE_SIZE)

        val cclClient = client.newBuilder()
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .client(cclClient)
            .baseUrl(url)
            .build()
            .create(CCLConfigurationApiV1::class.java)
    }
}

private const val CCL_DIR = "ccl"
private const val CCL_CACHE_DIR = "ccl_config_http_cache"
private const val CACHE_SIZE = 5 * 1024 * 1024L // 5MB

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CCLConfiguration
