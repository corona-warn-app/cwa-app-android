package de.rki.coronawarnapp.datadonation.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata.ClientMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindowDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionModule
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsPCRKeySubmissionDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsRAKeySubmissionDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsExposureWindowsSettings
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsPCRTestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsRATestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultModule
import de.rki.coronawarnapp.datadonation.analytics.modules.usermetadata.UserMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsApiV1
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.DefaultLastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
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
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(
    includes = [
        AnalyticsModule.BindsModule::class,
        AnalyticsModule.ResetModule::class,
        AnalyticsKeySubmissionModule::class,
        AnalyticsTestResultModule::class
    ]
)
object AnalyticsModule {

    @Reusable
    @Provides
    fun provideAnalyticsSubmissionApi(
        @DataDonationCDNHttpClient client: OkHttpClient,
        @DataDonationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): DataDonationAnalyticsApiV1 {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .addConverterFactory(protoConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(DataDonationAnalyticsApiV1::class.java)
    }

    @Singleton
    @AnalyticsSettingsDataStore
    @Provides
    fun provideAnalyticsSettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_ANALYTICS_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ANALYTICS_SETTINGS_NAME
            )
        )
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableAnalytics(resettable: Analytics): Resettable

        @Binds
        @IntoSet
        fun bindResettableAnalyticsSettings(resettable: AnalyticsSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableAnalyticsExposureWindowsSettings(resettable: AnalyticsExposureWindowsSettings): Resettable
    }

    @Module
    internal interface BindsModule {

        @IntoSet
        @Binds
        fun newExposureWindows(module: AnalyticsExposureWindowDonor): DonorModule

        @IntoSet
        @Binds
        fun pcrKeySubmission(module: AnalyticsPCRKeySubmissionDonor): DonorModule

        @IntoSet
        @Binds
        fun raKeySubmission(module: AnalyticsRAKeySubmissionDonor): DonorModule

        @IntoSet
        @Binds
        fun pcrTestResult(module: AnalyticsPCRTestResultDonor): DonorModule

        @IntoSet
        @Binds
        fun raTestResult(module: AnalyticsRATestResultDonor): DonorModule

        @IntoSet
        @Binds
        fun exposureRiskMetadata(module: ExposureRiskMetadataDonor): DonorModule

        @IntoSet
        @Binds
        fun userMetadata(module: UserMetadataDonor): DonorModule

        @IntoSet
        @Binds
        fun clientMetadata(module: ClientMetadataDonor): DonorModule

        @Binds
        fun analyticsLogger(logger: DefaultLastAnalyticsSubmissionLogger): LastAnalyticsSubmissionLogger
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalyticsSettingsDataStore

private const val LEGACY_SHARED_PREFS_ANALYTICS_SETTINGS_NAME = "analytics_localdata"
private const val STORAGE_DATASTORE_ANALYTICS_SETTINGS_NAME = "analytics_settings_storage"
