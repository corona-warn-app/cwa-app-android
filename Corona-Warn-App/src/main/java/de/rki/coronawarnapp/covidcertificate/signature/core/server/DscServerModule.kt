package de.rki.coronawarnapp.covidcertificate.signature.core.server

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.covidcertificate.validation.core.CertificateValidation
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@InstallIn(SingletonComponent::class)
@Module
class DscServerModule {

    @Reusable
    @Provides
    fun apiV1(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @CertificateValidation cache: Cache,
        jacksonConverterFactory: JacksonConverterFactory
    ): DscApiV1 = Retrofit.Builder()
        .client(
            httpClient.newBuilder()
                .cache(cache)
                .build()
        )
        .baseUrl(url)
        .addConverterFactory(jacksonConverterFactory)
        .build()
        .create(DscApiV1::class.java)
}
