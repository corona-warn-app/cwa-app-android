package de.rki.coronawarnapp.datadonation.survey

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.datadonation.survey.consent.SurveyConsentModule
import de.rki.coronawarnapp.datadonation.survey.server.SurveyApiV1
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Singleton

@Module(
    includes = [SurveyConsentModule::class]
)
class SurveyModule {

    @Singleton
    @Provides
    fun provideSurveyApi(
        @DataDonationCDNHttpClient client: OkHttpClient,
        @DataDonationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): SurveyApiV1 = Retrofit.Builder()
        .client(client.newBuilder().build())
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(SurveyApiV1::class.java)
}
