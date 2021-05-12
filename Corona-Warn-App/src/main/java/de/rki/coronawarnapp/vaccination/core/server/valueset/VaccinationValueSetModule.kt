package de.rki.coronawarnapp.vaccination.core.server.valueset

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.vaccination.VaccinationCertificateCDNUrl
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
    fun cache(
        @AppContext context: Context
    ): Cache {
        val cacheDir = File(context.cacheDir, "vaccination_value")
        val cacheFile = File(cacheDir, "http_cache")
        return Cache(cacheFile, CACHE_SIZE_5MB)
    }

    @Reusable
    @VaccinationValueSetHttpClient
    @Provides
    fun httpClient(
        @HttpClientDefault defaultHttpClient: OkHttpClient,
        @VaccinationValueSetHttpClient cache: Cache
    ): OkHttpClient = defaultHttpClient.newBuilder()
        .cache(cache)
        .build()

    @Reusable
    @Provides
    fun api(
        @VaccinationValueSetHttpClient httpClient: OkHttpClient,
        @VaccinationCertificateCDNUrl url: String
    ): VaccinationValueSetApiV1 = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(url)
        .build()
        .create(VaccinationValueSetApiV1::class.java)

    companion object {
        private const val CACHE_SIZE_5MB = 5 * 1024 * 1024L // 5MB
    }
}
