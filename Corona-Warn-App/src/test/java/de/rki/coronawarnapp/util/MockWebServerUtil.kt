package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.util.security.VerificationKeys
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory

fun MockWebServer.newWebRequestBuilder(): WebRequestBuilder {
    val httpClient = OkHttpClient.Builder()
        .build()

    val retrofit = Retrofit.Builder()
        .client(httpClient)
        .addConverterFactory(ProtoConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())

    return WebRequestBuilder(
        retrofit.baseUrl(this.url("/distribution/")).build()
            .create(DistributionService::class.java),
        retrofit.baseUrl(this.url("/verification/")).build()
            .create(VerificationService::class.java),
        retrofit.baseUrl(this.url("/submission/")).build()
            .create(SubmissionService::class.java),
        VerificationKeys()
    )
}