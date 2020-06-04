package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
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
        HttpLoggingInterceptor().also {
            if (BuildConfig.DEBUG) it.setLevel(HttpLoggingInterceptor.Level.BODY)
        },
        OfflineCacheInterceptor(CoronaWarnApplication.getAppContext()),
        RetryInterceptor()
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

        val timeoutMs = TimeVariables.getTransactionTimeout()
        clientBuilder.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        clientBuilder.readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        clientBuilder.writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        clientBuilder.callTimeout(timeoutMs, TimeUnit.MILLISECONDS)

        clientBuilder.connectionPool(conPool)

        cache.evictAll()
        clientBuilder.cache(cache)

        val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.RESTRICTED_TLS)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
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

        clientBuilder.connectionSpecs(listOf(spec))

        CertificatePinnerFactory().getCertificatePinner().run {
            if (this.pins.isNotEmpty()) {
                clientBuilder.certificatePinner(this)
            }
        }

        mInterceptors.forEach { clientBuilder.addInterceptor(it) }

        clientBuilder.build()
    }

    fun distributionService(): DistributionService = distributionService
    private val distributionService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(DynamicURLs.DOWNLOAD_CDN_URL)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(DistributionService::class.java)
    }

    fun verificationService(): VerificationService = verificationService
    private val verificationService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(DynamicURLs.VERIFICATION_CDN_URL)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(VerificationService::class.java)
    }

    fun submissionService(): SubmissionService = submissionService
    private val submissionService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(DynamicURLs.SUBMISSION_CDN_URL)
            .addConverterFactory(protoConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(SubmissionService::class.java)
    }
}
