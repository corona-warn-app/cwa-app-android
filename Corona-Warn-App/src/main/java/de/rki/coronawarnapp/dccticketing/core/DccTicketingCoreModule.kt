package de.rki.coronawarnapp.dccticketing.core

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingApiV1
import de.rki.coronawarnapp.http.HttpErrorParser
import de.rki.coronawarnapp.http.config.HTTPVariables
import de.rki.coronawarnapp.http.interceptor.RetryInterceptor
import de.rki.coronawarnapp.http.interceptor.WebSecurityVerificationInterceptor
import de.rki.coronawarnapp.risk.TimeVariables
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Module
class DccTicketingCoreModule {

    @DccTicketingHttpClient
    @Provides
    fun provideHttpClient(): OkHttpClient {
        val interceptors: List<Interceptor> = listOf(
            WebSecurityVerificationInterceptor(),
            HttpLoggingInterceptor { message -> Timber.tag("OkHttp").v(message) }.apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            },
            RetryInterceptor(),
            HttpErrorParser()
        )

        return OkHttpClient.Builder().apply {
            connectTimeout(HTTPVariables.getHTTPConnectionTimeout(), TimeUnit.MILLISECONDS)
            readTimeout(HTTPVariables.getHTTPReadTimeout(), TimeUnit.MILLISECONDS)
            writeTimeout(HTTPVariables.getHTTPWriteTimeout(), TimeUnit.MILLISECONDS)
            callTimeout(TimeVariables.getTransactionTimeout(), TimeUnit.MILLISECONDS)

            interceptors.forEach { addInterceptor(it) }
        }.build()
    }

    @Reusable
    @Provides
    fun provideDccTicketingValidationApiV1(
        @DccTicketingHttpClient client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): DccTicketingApiV1 = Retrofit.Builder()
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .baseUrl(BASE_URL)
        .build()
        .create(DccTicketingApiV1::class.java)
}

// Dummy base url to satisfy Retrofit ¯\_(ツ)_/¯
private const val BASE_URL = "https://localhost.de"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DccTicketing
