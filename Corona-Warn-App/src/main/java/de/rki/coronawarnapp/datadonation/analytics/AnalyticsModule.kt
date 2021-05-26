package de.rki.coronawarnapp.datadonation.analytics

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata.ClientMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata.ExposureRiskMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsExposureWindowDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsPcrKeySubmissionDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsRaKeySubmissionDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsPCRTestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsRATestResultDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.usermetadata.UserMetadataDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsApiV1
import de.rki.coronawarnapp.datadonation.analytics.storage.DefaultLastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Singleton

@Module
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
    fun pcrKeySubmission(module: AnalyticsPcrKeySubmissionDonor): DonorModule = module

    @IntoSet
    @Provides
    fun raKeySubmission(module: AnalyticsRaKeySubmissionDonor): DonorModule = module

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
}
