package de.rki.coronawarnapp.dccreissuance.core.server

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.environment.dccreissuance.DccReissuanceServerURL
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.http.HttpErrorParser
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.net.URL

@Module
object DccReissuanceServerModule {

    @Provides
    fun provideApi(
        @DccReissuanceServerURL url: String,
        @HttpClientDefault defaultClient: OkHttpClient,
        appConfigProvider: AppConfigProvider,
        gsonConverterFactory: GsonConverterFactory
    ): DccReissuanceApi = runBlocking {

        val reissueServicePublicKeyDigest = appConfigProvider.currentConfig.first()
            .covidCertificateParameters
            .reissueServicePublicKeyDigest
            .base64()

        val certificatePinner = CertificatePinner.Builder().add(
            URL(url).host, "sha256/$reissueServicePublicKeyDigest"
        ).build()

        val client = defaultClient.newBuilder()
            .apply {
                certificatePinner(certificatePinner)
                // Remove http error parser for custom error handling
                interceptors()
                    .removeAll { it is HttpErrorParser }
                    .also { Timber.tag(TAG).d("Removed %s? %b", tag<HttpErrorParser>(), it) }
            }
            .build()

        Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(DccReissuanceApi::class.java)
    }

    private val TAG = tag<DccReissuanceServerModule>()
}
