package de.rki.coronawarnapp.vaccination.core.server.valueset

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.vaccination.VaccinationCertificateValueSetCDNUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
class VaccinationValueSetModule {

    @Reusable
    @VaccinationValueSetHttpClient
    @Provides
    fun httpClient(
        @HttpClientDefault defaultHttpClient: OkHttpClient
    ): OkHttpClient = defaultHttpClient.newBuilder()
        .build()

    @Reusable
    @Provides
    fun api(
        @VaccinationValueSetHttpClient httpClient: OkHttpClient,
        @VaccinationCertificateValueSetCDNUrl url: String
    ): VaccinationValueSetApiV1 = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(url)
        .build()
        .create(VaccinationValueSetApiV1::class.java)
}
