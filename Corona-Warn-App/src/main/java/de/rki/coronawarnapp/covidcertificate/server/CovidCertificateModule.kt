package de.rki.coronawarnapp.covidcertificate.server

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.covidcertificate.DCCHttpClient
import de.rki.coronawarnapp.environment.covidcertificate.DCCServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class CovidCertificateModule {

    @Reusable
    @Provides
    fun apiV1(
        @DCCHttpClient httpClient: OkHttpClient,
        @DCCServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): CovidCertificateApiV1 {
        return Retrofit.Builder()
            .client(httpClient)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(CovidCertificateApiV1::class.java)
    }
}
