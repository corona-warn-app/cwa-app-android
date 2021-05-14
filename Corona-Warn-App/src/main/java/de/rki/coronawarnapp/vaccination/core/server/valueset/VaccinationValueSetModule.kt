package de.rki.coronawarnapp.vaccination.core.server.valueset

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File

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
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .build()
            .create(VaccinationValueSetApiV1::class.java)
    }

    companion object {
        private const val CACHE_SIZE_5MB = 5 * 1024 * 1024L // 5MB
    }
}
