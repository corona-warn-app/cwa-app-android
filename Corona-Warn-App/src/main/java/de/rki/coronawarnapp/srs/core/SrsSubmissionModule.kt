package de.rki.coronawarnapp.srs.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import de.rki.coronawarnapp.environment.submission.SubmissionCDNServerUrl
import de.rki.coronawarnapp.srs.core.server.SrsAuthorizationApi
import de.rki.coronawarnapp.srs.core.server.SrsSubmissionApi
import de.rki.coronawarnapp.submission.DEFAULT_CACHE_SIZE
import de.rki.coronawarnapp.submission.server.SubmissionHttpClient
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(
    includes = [
        SrsDevSettingsModule::class
    ]
)
object SrsSubmissionModule {

    @Singleton
    @Provides
    fun provideSrsAuthorizationApi(
        @DataDonationCDNHttpClient client: OkHttpClient,
        @DataDonationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory
    ): SrsAuthorizationApi = Retrofit.Builder()
        .client(client.newBuilder().build())
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .build()
        .create(SrsAuthorizationApi::class.java)

    @Singleton
    @Provides
    fun provideSrsSubmissionApi(
        @AppContext context: Context,
        @SubmissionHttpClient client: OkHttpClient,
        @SubmissionCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
    ): SrsSubmissionApi {
        val cache = Cache(File(context.cacheDir, "http_submission"), DEFAULT_CACHE_SIZE)
        val cachingClient = client.newBuilder().apply { cache(cache) }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(protoConverterFactory)
            .build()
            .create(SrsSubmissionApi::class.java)
    }

    @Singleton
    @Provides
    @SrsSettingsDataStore
    fun provideSrsSettingsDataStore(@AppContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("srs_settings_localdata")
        }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class SrsSettingsDataStore
