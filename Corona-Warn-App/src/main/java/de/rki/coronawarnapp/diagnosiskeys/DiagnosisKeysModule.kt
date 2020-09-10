package de.rki.coronawarnapp.diagnosiskeys

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.diagnosiskeys.server.AppConfigApiV1
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyApiV1
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadHomeCountry
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadHttpClient
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadServerUrl
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.KeyCacheLegacyDao
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.storage.AppDatabase
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
class DiagnosisKeysModule {

    @Singleton
    @DownloadHomeCountry
    @Provides
    fun provideDiagnosisHomeCountry(): LocationCode = LocationCode("DE")

    @Reusable
    @DownloadHttpClient
    @Provides
    fun cdnHttpClient(@HttpClientDefault defaultHttpClient: OkHttpClient): OkHttpClient =
        defaultHttpClient.newBuilder().connectionSpecs(CDN_CONNECTION_SPECS).build()

    @Singleton
    @Provides
    fun provideDiagnosisKeyApi(
        @DownloadHttpClient client: OkHttpClient,
        @DownloadServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): DiagnosisKeyApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(DiagnosisKeyApiV1::class.java)

    @Singleton
    @Provides
    fun provideAppConfigApi(
        context: Context,
        @DownloadHttpClient client: OkHttpClient,
        @DownloadServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): AppConfigApiV1 {
        val cacheSize = 1 * 1024 * 1024L // 1MB
        val cacheDir = File(context.cacheDir, "http_app-config")
        val cache = Cache(cacheDir, cacheSize)
        val cachingClient = client.newBuilder().cache(cache).build()
        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(AppConfigApiV1::class.java)
    }

    @Singleton
    @DownloadServerUrl
    @Provides
    fun provideDownloadServerUrl(): String {
        val url = BuildConfig.DOWNLOAD_CDN_URL
        if (!url.startsWith("https://")) throw IllegalStateException("Innvalid: $url")
        return url
    }

    @Singleton
    @Provides
    fun legacyKeyCacheDao(context: Context): KeyCacheLegacyDao {
        return AppDatabase.getInstance(context).dateDao()
    }

    companion object {
        private val CDN_CONNECTION_SPECS = listOf(
            ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(
                    TlsVersion.TLS_1_0,
                    TlsVersion.TLS_1_1,
                    TlsVersion.TLS_1_2,
                    TlsVersion.TLS_1_3
                )
                .allEnabledCipherSuites()
                .build()
        )
    }
}
