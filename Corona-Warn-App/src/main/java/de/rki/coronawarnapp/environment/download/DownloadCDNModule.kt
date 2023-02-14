package de.rki.coronawarnapp.environment.download

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DownloadCDNModule : BaseEnvironmentModule() {

    @Reusable
    @DownloadCDNHttpClient
    @Provides
    fun cdnHttpClient(@HttpClientDefault defaultHttpClient: OkHttpClient): OkHttpClient =
        defaultHttpClient.newBuilder().connectionSpecs(DOWNLOAD_CDN_CONNECTION_SPECS).build()

    @Singleton
    @DownloadCDNServerUrl
    @Provides
    fun provideDownloadServerUrl(environment: EnvironmentSetup): String {
        val url = environment.downloadCdnUrl
        return requireValidUrl(url)
    }

    companion object {
        private val DOWNLOAD_CDN_CONNECTION_SPECS = ConnectionSpec
            .Builder(ConnectionSpec.COMPATIBLE_TLS)
            .tlsVersions(
                TlsVersion.TLS_1_0,
                TlsVersion.TLS_1_1,
                TlsVersion.TLS_1_2,
                TlsVersion.TLS_1_3
            )
            .allEnabledCipherSuites()
            .build()
            .let { listOf(it) }
    }
}
