package de.rki.coronawarnapp.dccreissuance.core.server

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.environment.dccreissuance.DccReissuanceServerURL
import de.rki.coronawarnapp.http.HttpErrorParser
import de.rki.coronawarnapp.http.config.HTTPVariables
import de.rki.coronawarnapp.http.interceptor.RetryInterceptor
import de.rki.coronawarnapp.http.interceptor.WebSecurityVerificationInterceptor
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpClientReissuance

@Module
object DccReissuanceServerModule {

    @Provides
    fun provideApi(
        @DccReissuanceServerURL url: String,
        @HttpClientReissuance defaultClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): DccReissuanceApi {
        val client = defaultClient.newBuilder()
            .apply {
                // Remove http error parser for custom error handling
                interceptors()
                    .removeAll { it is HttpErrorParser }
                    .also { Timber.tag(TAG).d("Removed %s? %b", tag<HttpErrorParser>(), it) }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(DccReissuanceApi::class.java)
    }

    @Reusable
    @HttpClientReissuance
    @Provides
    fun httpClient(
        appConfigProvider: AppConfigProvider,
        @DccReissuanceServerURL url: String,
    ): OkHttpClient = runBlocking {

        val reissueServicePublicKeyDigest = appConfigProvider.currentConfig.first()
            .covidCertificateParameters
            .reissueServicePublicKeyDigest
            .base64()

        val certificatePinner = CertificatePinner.Builder().add(
            URL(url).host, "sha256/$reissueServicePublicKeyDigest"
        ).build()
        val interceptors: List<Interceptor> = listOf(
            WebSecurityVerificationInterceptor(),
            HttpLoggingInterceptor { message -> Timber.tag("OkHttp").v(message) }.apply {
                if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
            },
            RetryInterceptor(),
            HttpErrorParser()
        )

        OkHttpClient.Builder().apply {
            connectTimeout(HTTPVariables.getHTTPConnectionTimeout(), TimeUnit.MILLISECONDS)
            readTimeout(HTTPVariables.getHTTPReadTimeout(), TimeUnit.MILLISECONDS)
            writeTimeout(HTTPVariables.getHTTPWriteTimeout(), TimeUnit.MILLISECONDS)
            callTimeout(TimeVariables.getTransactionTimeout(), TimeUnit.MILLISECONDS)
            certificatePinner(certificatePinner)
            interceptors.forEach { addInterceptor(it) }
        }.build()
    }

    private val TAG = tag<DccReissuanceServerModule>()
}
