package de.rki.coronawarnapp.datadonation.survey

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.datadonation.survey.consent.SurveyConsentModule
import de.rki.coronawarnapp.datadonation.survey.server.DataDonationApiV1
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import de.rki.coronawarnapp.util.di.AppContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.File
import javax.inject.Singleton

@Module(
    includes = [SurveyConsentModule::class]
)
class SurveyModule {

    @Singleton
    @Provides
    fun provideDataDonationApi(
        @AppContext context: Context,
        @DataDonationCDNHttpClient client: OkHttpClient,
        @DataDonationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): DataDonationApiV1 {
        val cache = Cache(File(context.cacheDir, "http_data_donation"), DEFAULT_CACHE_SIZE)

        val cachingClient = client.newBuilder().apply {
            cache(cache)
        }.build()

        return Retrofit.Builder()
            .client(cachingClient)
            .baseUrl(url)
            .addConverterFactory(protoConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(DataDonationApiV1::class.java)
    }
}

private const val DEFAULT_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
