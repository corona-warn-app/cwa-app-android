package de.rki.coronawarnapp.http

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory

@Module
class HttpModule {

    @Reusable
    @HttpClientDefault
    @Provides
    fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder().build(BuildConfig.DEBUG)

    @Reusable
    @Provides
    fun provideGSONConverter(): GsonConverterFactory = GsonConverterFactory.create()

    @Reusable
    @Provides
    fun provideProtoConverter(): ProtoConverterFactory = ProtoConverterFactory.create()

    @Reusable
    @RestrictedConnectionSpecs
    @Provides
    fun restrictedConnectionSpecs(): List<ConnectionSpec> = ConnectionSpec
        .Builder(ConnectionSpec.RESTRICTED_TLS)
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
        .let { listOf(it) }
}
