package de.rki.coronawarnapp.diagnosiskeys

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyApiV1
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.KeyCacheLegacyDao
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.util.di.AppContext
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

    @Singleton
    @Provides
    fun legacyKeyCacheDao(@AppContext context: Context): KeyCacheLegacyDao {
        return AppDatabase.getInstance(context).dateDao()
    }
}
