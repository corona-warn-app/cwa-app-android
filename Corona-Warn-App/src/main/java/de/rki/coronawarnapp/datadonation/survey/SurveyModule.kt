package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.datadonation.survey.consent.SurveyConsentModule
import de.rki.coronawarnapp.datadonation.survey.server.SurveyApiV1
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(
    includes = [SurveyModule.ResetModule::class, SurveyConsentModule::class]
)
object SurveyModule {

    @Singleton
    @Provides
    fun provideSurveyApi(
        @DataDonationCDNHttpClient client: OkHttpClient,
        @DataDonationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        jacksonConverterFactory: JacksonConverterFactory
    ): SurveyApiV1 = Retrofit.Builder()
        .client(client.newBuilder().build())
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .addConverterFactory(jacksonConverterFactory)
        .build()
        .create(SurveyApiV1::class.java)

    @Singleton
    @SurveySettingsDataStore
    @Provides
    fun provideSurveySettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_SURVEY_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_SURVEY_SETTINGS_NAME
            )
        )
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableSurveySettings(resettable: SurveySettings): Resettable
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class SurveySettingsDataStore

private const val LEGACY_SHARED_PREFS_SURVEY_SETTINGS_NAME = "survey_localdata"
private const val STORAGE_DATASTORE_SURVEY_SETTINGS_NAME = "survey_localdata_storage"
