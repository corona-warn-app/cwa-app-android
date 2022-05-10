package de.rki.coronawarnapp.datadonation.analytics

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata.ClientMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindowDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsPCRKeySubmissionDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsRAKeySubmissionDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsExposureWindowsSettings
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsPCRTestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsRATestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.usermetadata.UserMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsApiV1
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.DefaultLastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory

@Module(includes = [AnalyticsModule.BindsModule::class, AnalyticsModule.ResetModule::class])
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
