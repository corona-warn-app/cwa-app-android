package de.rki.coronawarnapp.vaccination.core.server.valueset

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

@Module
class VaccinationValueSetModule {

    @Reusable
    @ValueSet
    @Provides
    fun cache(
        @AppContext context: Context
    ): Cache {
        val cacheDir = File(context.cacheDir, "vaccination_value")
        return Cache(cacheDir, CACHE_SIZE_5MB)
    }

    @Reusable
    @Provides
    fun api(
        @DownloadCDNHttpClient httpClient: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        @ValueSet cache: Cache
    ): VaccinationValueSetApiV1 {
        val client = httpClient.newBuilder()
            .addNetworkInterceptor(CacheInterceptor())
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .build()
            .create(VaccinationValueSetApiV1::class.java)
    }

    private class CacheInterceptor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())

            val cacheControl = CacheControl.Builder()
                .maxAge(300, TimeUnit.SECONDS)
                .build()

            // We cache as we please
            val cacheHeader = "Cache-Control"
            return response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader(cacheHeader)
                .addHeader(cacheHeader, cacheControl.toString())
                .build()
        }
    }

    companion object {
        private const val CACHE_SIZE_5MB = 5 * 1024 * 1024L // 5MB
    }
}
