package de.rki.coronawarnapp.diagnosiskeys

import android.webkit.URLUtil
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
    fun provideCDNHomeCountry(): LocationCode = LocationCode("DE")

    @Reusable
    @DownloadHttpClient
    @Provides
    fun cdnHttpClient(@HttpClientDefault defaultHttpClient: OkHttpClient): OkHttpClient =
        defaultHttpClient.newBuilder().connectionSpecs(CDN_CONNECTION_SPECS).build()

    @Singleton
    @Provides
    fun cdnApi(
        @DownloadHttpClient cdnHttpClient: OkHttpClient,
        @DownloadServerUrl cdnUrl: String,
        gsonConverterFactory: GsonConverterFactory
    ): DownloadApiV1 = Retrofit.Builder()
        .client(cdnHttpClient)
        .baseUrl(cdnUrl)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(DownloadApiV1::class.java)

    @Singleton
    @DownloadServerUrl
    @Provides
    fun provideCDNUrl(): String = BuildConfig.DOWNLOAD_CDN_URL.also {
        if (!URLUtil.isHttpsUrl(it)) throw IllegalArgumentException("the url is invalid")
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
