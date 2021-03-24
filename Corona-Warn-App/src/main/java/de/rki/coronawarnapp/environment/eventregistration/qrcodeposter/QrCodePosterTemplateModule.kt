package de.rki.coronawarnapp.environment.eventregistration.qrcodeposter

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate.QrCodePosterTemplateApiV1
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
interface QrCodePosterTemplateModule {

    @Singleton
    @Provides
    @QrCodePoster
    fun cacheDir(
        @AppContext context: Context
    ): File = File(context.cacheDir, "qrCodePoster")

    @Singleton
    @Provides
    @QrCodePoster
    fun httpCache(
        @QrCodePoster cacheDir: File
    ): Cache = Cache(File(cacheDir, "cache_http"), CACHE_SIZE_5MB)

    @Singleton
    @Provides
    fun api(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        @QrCodePoster cache: Cache
    ): QrCodePosterTemplateApiV1 {
        val httpClient = client.newBuilder().apply {
            cache(cache)
        }.build()

        return Retrofit.Builder()
            .client(httpClient)
            .baseUrl(url)
            .addConverterFactory(protoConverterFactory)
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
annotation class QrCodePoster
