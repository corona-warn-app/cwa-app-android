package de.rki.coronawarnapp.http.request

import KeyExportFormat
import android.util.Log
import com.android.volley.Cache.Entry
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import de.rki.coronawarnapp.exception.ExceptionCategory.HTTP
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.util.ZipHelper.unzip

class ApplicationConfigurationRequest(
    url: String,
    private val requestTag: Any?,
    private val headers: MutableMap<String, String>?,
    private val listener: Response.Listener<ApplicationConfiguration>,
    errorListener: Response.ErrorListener
) : Request<ApplicationConfiguration>(Method.GET, url, errorListener) {

    companion object {
        private val TAG: String? = ApplicationConfigurationRequest::class.simpleName

        private const val SOFT_TTL =
            5 * 60 * 1000 // in 5 minutes cache will be hit, but also refreshed on background
        private const val TTL = 1 * 60 * 60 * 1000 // in 1 hours this cache entry expires completely
    }

    override fun getTag(): Any = requestTag ?: super.getTag().also { this.addMarker("tag:$it") }
    override fun deliverResponse(response: ApplicationConfiguration) = listener.onResponse(response)
    override fun getHeaders(): MutableMap<String, String> = headers ?: super.getHeaders()

    override fun parseNetworkResponse(response: NetworkResponse?): Response<ApplicationConfiguration> {
        return try {
            var cacheEntry = HttpHeaderParser.parseCacheHeaders(response)
            if (cacheEntry == null && response != null) {
                Log.v(TAG, "new cache entry.")
                cacheEntry = Entry()
                cacheEntry.data = response.data
                cacheEntry.softTtl = SOFT_TTL.toLong()
                cacheEntry.ttl = TTL.toLong()
                cacheEntry.responseHeaders = response.headers
            } else {
                Log.v(TAG, "using cache entry")
            }

            var applicationConfiguration: ApplicationConfiguration? = null
            response!!.data.inputStream().unzip { entry, entryContent ->
                if (entry.name == "export.bin") {
                    val appConfig = ApplicationConfiguration.parseFrom(entryContent)
//                    Log.d(TAG, "app config from zip: $appConfig")
                    applicationConfiguration = appConfig
                }
                if (entry.name == "export.sig") {
                    val signatures = KeyExportFormat.TEKSignatureList.parseFrom(entryContent)
                    signatures.signaturesList.forEach {
                        Log.d(TAG, it.signatureInfo.toString())
                    }
                }
            }
            if (applicationConfiguration == null) {
                throw IllegalArgumentException("no file was found in the downloaded zip")
            }

            Response.success(applicationConfiguration, cacheEntry)
        } catch (e: Exception) {
            e.report(
                HTTP,
                requestTag.toString(),
                null
            )
            Response.error(ParseError(e))
        }
    }
}
