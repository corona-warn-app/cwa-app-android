package de.rki.coronawarnapp.http

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.http.config.HTTPVariables
import de.rki.coronawarnapp.http.interceptor.RetryInterceptor
import de.rki.coronawarnapp.http.interceptor.WebSecurityVerificationInterceptor
import de.rki.coronawarnapp.risk.TimeVariables
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Module
class HttpModule {

    @Reusable
    @HttpClientDefault
    @Provides
    fun defaultHttpClient(): OkHttpClient {
        val interceptors: List<Interceptor> = listOf(
            WebSecurityVerificationInterceptor(),
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Timber.tag("OkHttp").v(message)
                }
            }).apply {
                if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
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
    fun provideGSONConverter(): GsonConverterFactory = GsonConverterFactory.create()

    @Reusable
    @Provides
    fun provideProtoonverter(): ProtoConverterFactory = ProtoConverterFactory.create()
}
