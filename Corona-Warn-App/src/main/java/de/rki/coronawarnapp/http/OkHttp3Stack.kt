package de.rki.coronawarnapp.http

import android.content.Context
import com.android.volley.Header
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.HttpResponse
import de.rki.coronawarnapp.risk.TimeVariables
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.TlsVersion
import java.io.File
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Convenience Wrapper used for accessing the OkHTTP Transport layer with the Volley Interfaces.
 * Designed for:
 * - in-memory connection management
 * - disk based caching (10MB)
 * - extension based on volleys BaseHttpStack
 * - volley request queue
 * - synchronous calls executed by asynchronous thread-pool
 *
 * @param context
 * @param interceptors
 */
class OkHttp3Stack(context: Context, interceptors: List<Interceptor>) : BaseHttpStack() {
    constructor(context: Context) : this(context, emptyList())

    /**
     * List of interceptors, e.g. logging
     */
    private val mInterceptors: List<Interceptor> = interceptors

    /**
     * connection pool held in-memory, especially useful for key retrieval
     */
    private val conPool = ConnectionPool()

    /**
     * Basic disk cache backed by LRU
     */
    private val cache = Cache(
        directory = File(context.cacheDir, HTTP_CACHE_NAME),
        maxSize = HTTP_CACHE_SIZE
    )

    /**
     * lazily initialized client instance of OkHTTP
     */
    private val client by lazy { buildClient() }

    /**
     * Convenience method to map headers from volley to OkHTTP style.
     */
    private fun mapHeaders(responseHeaders: Headers): List<Header> =
        responseHeaders.map { Header(it.first, it.second) }

    companion object {
        /**
         * 10 MiB
         */
        private const val HTTP_CACHE_SIZE = 50L * 1024L * 1024L

        /**
         * Cache file name
         */
        private const val HTTP_CACHE_NAME = "http_cache"

        /**
         * Convenience method used for building the correct request type.
         */
        private fun setConnectionParametersForRequest(
            builder: okhttp3.Request.Builder,
            request: Request<*>
        ) {
            when (request.method) {
                Request.Method.DEPRECATED_GET_OR_POST -> { throw IllegalArgumentException("deprecated.") }
                Request.Method.GET -> builder.get()
                Request.Method.DELETE -> builder.delete(createRequestBody(request))
                Request.Method.POST -> builder.post(createRequestBody(request)!!)
                Request.Method.PUT -> builder.put(createRequestBody(request)!!)
                Request.Method.HEAD -> builder.head()
                Request.Method.OPTIONS -> builder.method("OPTIONS", null)
                Request.Method.TRACE -> builder.method("TRACE", null)
                Request.Method.PATCH -> builder.patch(createRequestBody(request)!!)
                else -> throw IllegalStateException("Unknown method type.")
            }
        }

        /**
         * Convenience method to create a request body based on MediaType
         *
         * @param volleyRequest
         */
        private fun createRequestBody(volleyRequest: Request<*>): RequestBody? =
            volleyRequest.body.toRequestBody(
                volleyRequest.bodyContentType.toMediaType(),
                0,
                volleyRequest.body.size
            )
    }

    override fun executeRequest(
        request: Request<*>,
        additionalHeaders: MutableMap<String, String>?
    ): HttpResponse {
        val okHttpRequest = buildRequest(request, additionalHeaders)

        val okHttpCall = client.newCall(okHttpRequest)
        val okHttpResponse = okHttpCall.execute()

        val code = okHttpResponse.code
        val body = okHttpResponse.body
        val content = body?.byteStream()
        val contentLength = body?.contentLength()?.toInt() ?: 0
        val responseHeaders = mapHeaders(okHttpResponse.headers)
        return HttpResponse(code, responseHeaders, contentLength, content)
    }

    /**
     * Wrapper around the OkHTTP request builder used to set header fields, url and connection params.
     *
     * @param request
     * @param additionalHeaders
     */
    private fun buildRequest(request: Request<*>, additionalHeaders: MutableMap<String, String>?):
            okhttp3.Request {
        val okHttpRequestBuilder = okhttp3.Request.Builder()
        okHttpRequestBuilder.url(request.url)

        val headers = request.headers
        headers.forEach {
            okHttpRequestBuilder.addHeader(it.key, it.value)
        }
        additionalHeaders?.forEach {
            okHttpRequestBuilder.addHeader(it.key, it.value)
        }

        setConnectionParametersForRequest(okHttpRequestBuilder, request)

        return okHttpRequestBuilder.build()
    }

    /**
     * Helper method used to build the client with connection pool, timeout values, caching,
     * connection specs, interceptors and other generic meta-info.
     */
    private fun buildClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

        val timeoutMs = TimeVariables.getTransactionTimeout()
        clientBuilder.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        clientBuilder.readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        clientBuilder.writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        clientBuilder.callTimeout(timeoutMs, TimeUnit.MILLISECONDS)

        clientBuilder.connectionPool(conPool)

        cache.evictAll()
        clientBuilder.cache(cache)

        val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
            .allEnabledCipherSuites() //TODO clarify more concrete Ciphers
            .build()

        clientBuilder.connectionSpecs(listOf(spec))

        // TODO add certificate pinning
//        val certificatePinner = CertificatePinner.Builder()
//            .add(
//                "x.de",
//                "sha256/base64"
//            )
//            .build()
//        clientBuilder.certificatePinner(certificatePinner)

        mInterceptors.forEach { clientBuilder.addInterceptor(it) }

        return clientBuilder.build()
    }
}
