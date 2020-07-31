package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.http.HttpErrorParser
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.interceptor.RetryInterceptor
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.util.security.VerificationKeys
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.utf8Size
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory

fun MockWebServer.newWebRequestBuilder(): WebRequestBuilder {
    val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(RetryInterceptor())
        .addInterceptor(HttpErrorParser())
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

fun RecordedRequest.requestHeaderWithoutContentLength() =
    listOf(this.requestLine)
        .plus(
            this.headers.filter { (k, _) -> k != "Content-Length" }.toString()
        )
        .joinToString("\n")

fun RecordedRequest.headerSizeIgnoringContentLength() =
    requestHeaderWithoutContentLength().utf8Size()