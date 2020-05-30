package de.rki.coronawarnapp.http.request

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.HttpHeaderParser
import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.exception.ExceptionCategory.HTTP
import de.rki.coronawarnapp.exception.report
import java.io.UnsupportedEncodingException

class KeySubmissionRequest(
    url: String,
    private val requestTag: Any?,
    private val binaryBody: ByteArray,
    private val authCode: String,
    private val faked: Boolean,
    private val retryPolicy: RetryPolicy?,
    private val listener: Response.Listener<Int>,
    errorListener: Response.ErrorListener
) : Request<Int>(Method.POST, url, errorListener) {

    companion object {
        private val TAG: String? = KeySubmissionRequest::class.simpleName
    }

    override fun getTag(): Any = requestTag ?: super.getTag().also { this.addMarker("tag:$it") }

    override fun getBodyContentType(): String =
        "application/x-protobuf".also { this.addMarker("bodyContentType:$it") }

    override fun getBody(): ByteArray = binaryBody

    override fun deliverResponse(response: Int) = listener.onResponse(response)

    override fun getRetryPolicy(): RetryPolicy =
        retryPolicy ?: (super.getRetryPolicy() ?: DefaultRetryPolicy())

    override fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>(super.getHeaders())
        if (faked) headers["cwa-fake"] = Math.random().toInt().toString()
        else headers["cwa-fake"] = "0"
        headers["cwa-authorization"] = authCode
        this.addMarker("headers:$headers")
        return headers
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<Int> {
        return try {
            if (response == null) throw NullPointerException("response is null, this should never be the case")
            return Response.success(
                response.statusCode,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            e.report(HTTP)
            Log.e(TAG, "invalid encoding found")
            Response.error(ParseError(e))
        } catch (e: InvalidProtocolBufferException) {
            Log.e(TAG, "invalid protobuf message, probably the object parse failed")
            e.report(HTTP)
            Response.error(ParseError(e))
        }
    }
}
