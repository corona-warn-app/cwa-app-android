package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.http.config.HTTPVariables
import de.rki.coronawarnapp.http.interceptor.RetryInterceptor
import de.rki.coronawarnapp.http.interceptor.WebSecurityVerificationInterceptor
import de.rki.coronawarnapp.risk.TimeVariables
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Creates OkHttpClient and enables logging based on the flag
 * @param enableLogging [Boolean]
 */
fun OkHttpClient.Builder.build(enableLogging: Boolean): OkHttpClient {
    val interceptors: List<Interceptor> = listOf(
        WebSecurityVerificationInterceptor(),
        HttpLoggingInterceptor { message -> Timber.tag("OkHttp").v(message) }.apply {
            if (enableLogging) setLevel(HttpLoggingInterceptor.Level.BODY)
        },
        RetryInterceptor(),
        HttpErrorParser()
    )

    return apply {
        connectTimeout(HTTPVariables.getHTTPConnectionTimeout(), TimeUnit.MILLISECONDS)
        readTimeout(HTTPVariables.getHTTPReadTimeout(), TimeUnit.MILLISECONDS)
        writeTimeout(HTTPVariables.getHTTPWriteTimeout(), TimeUnit.MILLISECONDS)
        callTimeout(TimeVariables.getTransactionTimeout(), TimeUnit.MILLISECONDS)

        interceptors.forEach { addInterceptor(it) }
    }.build()
}
