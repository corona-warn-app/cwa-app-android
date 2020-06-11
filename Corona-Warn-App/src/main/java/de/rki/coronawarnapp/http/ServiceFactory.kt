package de.rki.coronawarnapp.http

import android.webkit.URLUtil
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.http.ServiceFactoryException
import de.rki.coronawarnapp.http.config.HTTPVariables
import de.rki.coronawarnapp.http.interceptor.RetryInterceptor
import de.rki.coronawarnapp.http.interceptor.WebSecurityVerificationInterceptor
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.risk.TimeVariables
import okhttp3.Cache
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class ServiceFactory {
    companion object {
        /**
         * 10 MiB
         */
        private const val HTTP_CACHE_SIZE = 10L * 1024L * 1024L

        /**
         * Cache file name
         */
        private const val HTTP_CACHE_NAME = "http_cache"
    }

    /**
     * List of interceptors, e.g. logging
     */
    private val mInterceptors: List<Interceptor> = listOf(
        WebSecurityVerificationInterceptor(),
        HttpLoggingInterceptor().also {
            if (BuildConfig.DEBUG) it.setLevel(HttpLoggingInterceptor.Level.BODY)
        },
        RetryInterceptor(),
        HttpErrorParser()
    )

    /**
     * connection pool held in-memory, especially useful for key retrieval
     */
    private val conPool = ConnectionPool()

    /**
     * Basic disk cache backed by LRU
     */
    private val cache = Cache(
        directory = File(CoronaWarnApplication.getAppContext().cacheDir, HTTP_CACHE_NAME),
        maxSize = HTTP_CACHE_SIZE
    )

    private val gsonConverterFactory = GsonConverterFactory.create()
    private val protoConverterFactory = ProtoConverterFactory.create()

    private val okHttpClient by lazy {
        val clientBuilder = OkHttpClient.Builder()

        clientBuilder.connectTimeout(
            HTTPVariables.getHTTPConnectionTimeout(),
            TimeUnit.MILLISECONDS
        )
        clientBuilder.readTimeout(
            HTTPVariables.getHTTPReadTimeout(),
            TimeUnit.MILLISECONDS
        )
        clientBuilder.writeTimeout(
            HTTPVariables.getHTTPWriteTimeout(),
            TimeUnit.MILLISECONDS
        )
        clientBuilder.callTimeout(
            TimeVariables.getTransactionTimeout(),
            TimeUnit.MILLISECONDS
        )

        clientBuilder.connectionPool(conPool)

        cache.evictAll()
        clientBuilder.cache(cache)

        mInterceptors.forEach { clientBuilder.addInterceptor(it) }

        clientBuilder.build()
    }

    /**
     * For the CDN we want to ensure maximum Compatibility.
     */
    private fun getCDNSpecs(): List<ConnectionSpec> = listOf(
        ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .tlsVersions(
                TlsVersion.TLS_1_0,
                TlsVersion.TLS_1_1,
                TlsVersion.TLS_1_2,
                TlsVersion.TLS_1_3
            )
            .allEnabledCipherSuites()
            .build()
    )

    /**
     * For Submission and Verification we want to limit our specifications for TLS.
     */
    private fun getRestrictedSpecs(): List<ConnectionSpec> = listOf(
        ConnectionSpec.Builder(ConnectionSpec.RESTRICTED_TLS)
            .tlsVersions(
                TlsVersion.TLS_1_2,
                TlsVersion.TLS_1_3
            )
            .cipherSuites(
                // TLS 1.2 with Perfect Forward Secrecy (BSI TR-02102-2)
                CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_DSS_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                // TLS 1.3 (BSI TR-02102-2)
                CipherSuite.TLS_AES_128_GCM_SHA256,
                CipherSuite.TLS_AES_256_GCM_SHA384,
                CipherSuite.TLS_AES_128_CCM_SHA256
            )
            .build()
    )

    /**
     * Helper function to create a new client from an existent Client with New Specs.
     *
     * @param specs
     */
    private fun OkHttpClient.buildClientWithNewSpecs(specs: List<ConnectionSpec>) =
        this.newBuilder().connectionSpecs(specs).build()

    private val downloadCdnUrl
        get() = getValidUrl(BuildConfig.DOWNLOAD_CDN_URL)

    fun distributionService(): DistributionService = distributionService
    private val distributionService by lazy {
        Retrofit.Builder()
            .client(okHttpClient.buildClientWithNewSpecs(getCDNSpecs()))
            .baseUrl(downloadCdnUrl)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(DistributionService::class.java)
    }

    private val verificationCdnUrl
        get() = getValidUrl(BuildConfig.VERIFICATION_CDN_URL)

    fun verificationService(): VerificationService = verificationService
    private val verificationService by lazy {
        Retrofit.Builder()
            .client(okHttpClient.buildClientWithNewSpecs(getRestrictedSpecs()))
            .baseUrl(verificationCdnUrl)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(VerificationService::class.java)
    }

    private val submissionCdnUrl
        get() = getValidUrl(BuildConfig.SUBMISSION_CDN_URL)

    fun submissionService(): SubmissionService = submissionService
    private val submissionService by lazy {
        Retrofit.Builder()
            .client(okHttpClient.buildClientWithNewSpecs(getRestrictedSpecs()))
            .baseUrl(submissionCdnUrl)
            .addConverterFactory(protoConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(SubmissionService::class.java)
    }

    private fun getValidUrl(url: String): String {
        if (!URLUtil.isHttpsUrl(url)) {
            throw ServiceFactoryException(IllegalArgumentException("the url is invalid"))
        }
        return url
    }
}
