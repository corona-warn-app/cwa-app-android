package de.rki.coronawarnapp.diagnosiskeys

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyApiV1
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [DiagnosisKeysModule.ResetModule::class])
object DiagnosisKeysModule {

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

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableKeyCacheRepository(resettable: KeyCacheRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableDownloadDiagnosisKeysSettings(resettable: DownloadDiagnosisKeysSettings): Resettable
    }
}
