package de.rki.coronawarnapp.environment.eventregistration.createtracelocation

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.eventregistration.events.server.createtracelocation.CreateTraceLocationApiV1
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Singleton

@Module
class CreateTraceLocationModule : BaseEnvironmentModule() {

    @Reusable
    @TraceLocationCDNHttpClient
    @Provides
    fun cdnHttpClient(@HttpClientDefault okHttpClient: OkHttpClient): OkHttpClient = okHttpClient.newBuilder().build()

    @Singleton
    @CreateTraceLocationCDNServerUrl
    @Provides
    fun provideCreateTraceLocationCDNServerUrl(environment: EnvironmentSetup): String {
        val url = environment.traceLocationCdnUrl
        return requireValidUrl(url)
    }

    @Singleton
    @Provides
    fun provideCreateTraceLocationApi(
        @TraceLocationCDNHttpClient okHttpClient: OkHttpClient,
        @CreateTraceLocationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): CreateTraceLocationApiV1 =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(url)
            .addConverterFactory(protoConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(CreateTraceLocationApiV1::class.java)
}
