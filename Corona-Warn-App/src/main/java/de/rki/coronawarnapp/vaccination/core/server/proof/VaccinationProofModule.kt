package de.rki.coronawarnapp.vaccination.core.server.proof

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.vaccination.VaccinationCertificateProofServerUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
class VaccinationProofModule {

    @Reusable
    @VaccinationProofHttpClient
    @Provides
    fun httpClient(
        @HttpClientDefault default: OkHttpClient
    ): OkHttpClient = default.newBuilder().build()

    @Reusable
    @Provides
    fun api(
        @VaccinationProofHttpClient httpClient: OkHttpClient,
        @VaccinationCertificateProofServerUrl url: String,
        rawCOSEConverterFactory: RawCOSEObject.RetroFitConverterFactory,
    ): VaccinationProofApiV2 = Retrofit.Builder().apply {
        client(httpClient)
        baseUrl(url)
        addConverterFactory(rawCOSEConverterFactory)
    }.build().create(VaccinationProofApiV2::class.java)
}
