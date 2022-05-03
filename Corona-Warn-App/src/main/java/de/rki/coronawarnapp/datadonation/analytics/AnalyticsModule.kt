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
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsPCRTestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsRATestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.usermetadata.UserMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsApiV1
import de.rki.coronawarnapp.datadonation.analytics.storage.DefaultLastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Singleton

@Module(includes = [AnalyticsModule.BindsModule::class])
class AnalyticsModule {

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

    @IntoSet
    @Provides
    fun newExposureWindows(module: AnalyticsExposureWindowDonor): DonorModule = module

    @IntoSet
    @Provides
    fun pcrKeySubmission(module: AnalyticsPCRKeySubmissionDonor): DonorModule = module

    @IntoSet
    @Provides
    fun raKeySubmission(module: AnalyticsRAKeySubmissionDonor): DonorModule = module

    @IntoSet
    @Provides
    fun pcrTestResult(module: AnalyticsPCRTestResultDonor): DonorModule = module

    @IntoSet
    @Provides
    fun raTestResult(module: AnalyticsRATestResultDonor): DonorModule = module

    @IntoSet
    @Provides
    fun exposureRiskMetadata(module: ExposureRiskMetadataDonor): DonorModule = module

    @IntoSet
    @Provides
    fun userMetadata(module: UserMetadataDonor): DonorModule = module

    @IntoSet
    @Provides
    fun clientMetadata(module: ClientMetadataDonor): DonorModule = module

    @Provides
    @Singleton
    fun analyticsLogger(logger: DefaultLastAnalyticsSubmissionLogger): LastAnalyticsSubmissionLogger = logger

    @Module
    internal interface BindsModule {

        @Binds
        @IntoSet
        fun bindResettable(resettable: Analytics): Resettable
    }
}
