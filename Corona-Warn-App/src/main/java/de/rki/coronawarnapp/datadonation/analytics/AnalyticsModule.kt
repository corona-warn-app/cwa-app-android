package de.rki.coronawarnapp.datadonation.analytics

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.NewExposureWindowsDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.KeySubmissionStateDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest.RegisteredTestDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.risklevelhistory.RiskLevelHistoryChangesDonor
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsApiV1
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Singleton

@Module
class AnalyticsModule {

    @Singleton
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
    fun newExposureWindows(module: NewExposureWindowsDonor): DonorModule = module

    @IntoSet
    @Provides
    fun keySubmission(module: KeySubmissionStateDonor): DonorModule = module

    @IntoSet
    @Provides
    fun registeredTest(module: RegisteredTestDonor): DonorModule = module

    @IntoSet
    @Provides
    fun riskLevelHistory(module: RiskLevelHistoryChangesDonor): DonorModule = module
}
