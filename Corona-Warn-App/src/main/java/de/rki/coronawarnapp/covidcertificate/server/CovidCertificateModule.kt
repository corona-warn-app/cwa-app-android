package de.rki.coronawarnapp.covidcertificate.server

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
class CovidCertificateModule {

    // TODO check environment for DCC server
    @Reusable
    @Provides
    fun apiV1(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
    ): CovidCertificateApiV1 {
        return Retrofit.Builder()
            .client(httpClient)
            .baseUrl(url)
            .build()
            .create(CovidCertificateApiV1::class.java)
    }
}
