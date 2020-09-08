package de.rki.coronawarnapp.diagnosiskeys

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadApiV1
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadHomeCountry
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadHttpClient
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadServerUrl
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideDownloadApi(
        @DownloadHttpClient client: OkHttpClient,
        @DownloadServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): DownloadApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(DownloadApiV1::class.java)

    @Singleton
    @DownloadServerUrl
    @Provides
    fun provideDownloadServerUrl(): String {
        val url = BuildConfig.DOWNLOAD_CDN_URL
        if (!url.startsWith("https://")) throw IllegalStateException("Innvalid: $url")
        return url
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
