package de.rki.coronawarnapp.diagnosiskeys

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyApiV1
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class DiagnosisKeysModule {

    @Singleton
    @Provides
    fun provideDiagnosisKeyApi(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): DiagnosisKeyApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(DiagnosisKeyApiV1::class.java)

}
