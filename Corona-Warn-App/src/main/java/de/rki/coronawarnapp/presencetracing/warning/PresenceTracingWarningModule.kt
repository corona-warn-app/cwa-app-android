package de.rki.coronawarnapp.presencetracing.warning

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceTimeWarningApiV1
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class PresenceTracingWarningModule {

    @Singleton
    @Provides
    fun api(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory,
    ): TraceTimeWarningApiV1 {

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(TraceTimeWarningApiV1::class.java)
    }
}
