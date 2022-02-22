package de.rki.coronawarnapp.dccreissuance.core.server

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.vaccination.VaccinationProofServerUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
object DccReissuanceServerModule {

    @Provides
    fun provideApi(
        @VaccinationProofServerUrl url: String,
        @HttpClientDefault client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): DccReissuanceApi = Retrofit.Builder()
        .baseUrl(url)
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(DccReissuanceApi::class.java)
}
