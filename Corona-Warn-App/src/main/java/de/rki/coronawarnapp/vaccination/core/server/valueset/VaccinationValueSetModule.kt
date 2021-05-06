package de.rki.coronawarnapp.vaccination.core.server.valueset

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.vaccination.VaccinationCertificateValueSetCDNUrl
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File

@Module
class VaccinationValueSetModule {

    @Reusable
    @VaccinationValueSetHttpClient
    @Provides
    fun httpClient(
        @AppContext context: Context,
        @HttpClientDefault defaultHttpClient: OkHttpClient
    ): OkHttpClient {
        val cacheDir = File(context.cacheDir, "vaccination_value")
        val cache = Cache(File(cacheDir, "http_cache"), CACHE_SIZE_5MB)
        return defaultHttpClient.newBuilder()
            .cache(cache)
            .build()
    }

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

    companion object {
        private const val CACHE_SIZE_5MB = 5 * 1024 * 1024L // 5MB
    }
}
