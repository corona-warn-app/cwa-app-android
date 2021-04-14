package de.rki.coronawarnapp.environment.presencetracing.qrcodeposter

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate.QrCodePosterTemplateApiV1
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class QrCodePosterTemplateModule : BaseEnvironmentModule() {

    @Singleton
    @Provides
    @QrCodePosterTemplate
    fun cacheDir(
        @AppContext context: Context
    ): File = File(context.cacheDir, "poster")

    @Singleton
    @Provides
    @QrCodePosterTemplate
    fun httpCache(
        @QrCodePosterTemplate cacheDir: File
    ): Cache = Cache(File(cacheDir, "cache_http"), CACHE_SIZE_5MB)

    @Singleton
    @Provides
    fun api(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @QrCodePosterTemplate cache: Cache
    ): QrCodePosterTemplateApiV1 {
        val httpClient = client.newBuilder().apply {
            cache(cache)
        }.build()

        return Retrofit.Builder()
            .client(httpClient)
            .baseUrl(url)
            .build()
            .create(QrCodePosterTemplateApiV1::class.java)
    }

    companion object {
        private const val CACHE_SIZE_5MB = 5 * 1024 * 1024L
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class QrCodePosterTemplate
