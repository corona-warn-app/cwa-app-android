package de.rki.coronawarnapp.http

import android.webkit.URLUtil
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.http.ServiceFactoryException
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import okhttp3.Cache
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.io.File
import javax.inject.Inject

class ServiceFactory @Inject constructor(
    private val gsonConverterFactory: GsonConverterFactory,
    private val protoConverterFactory: ProtoConverterFactory,
    @HttpClientDefault private val defaultHttpClient: OkHttpClient
) {

    private val cache by lazy {
        Cache(
            directory = File(CoronaWarnApplication.getAppContext().cacheDir, HTTP_CACHE_FOLDER),
            maxSize = HTTP_CACHE_SIZE
        )
    }
    private val okHttpClient by lazy {
        defaultHttpClient
            .newBuilder()
            .connectionSpecs(getRestrictedSpecs())
            .cache(cache)
            .build()
    }

    private val verificationCdnUrl
        get() = getValidUrl(BuildConfig.VERIFICATION_CDN_URL)

    fun verificationService(): VerificationService = verificationService
    private val verificationService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
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
            .client(okHttpClient)
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

    companion object {
        private const val HTTP_CACHE_SIZE = 10L * 1024L * 1024L // 10 MiB
        private const val HTTP_CACHE_FOLDER = "http_cache" // <pkg>/cache/http_cache

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
    }
}
