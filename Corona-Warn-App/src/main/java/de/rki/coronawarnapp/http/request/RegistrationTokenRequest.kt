package de.rki.coronawarnapp.http.request

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.HttpHeaderParser
import de.rki.coronawarnapp.exception.ExceptionCategory.HTTP
import de.rki.coronawarnapp.exception.report
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.security.MessageDigest

class RegistrationTokenRequest(
    url: String,
    private val requestTag: Any?,
    private val keyType: String,
    private val key: String,
    private val faked: Boolean,
    private val retryPolicy: RetryPolicy?,
    private val listener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) : Request<String>(Method.POST, url, errorListener) {

    companion object {
        private val TAG: String? = RegistrationTokenRequest::class.simpleName
    }

    override fun getTag(): Any = requestTag ?: super.getTag().also { this.addMarker("tag:$it") }

    override fun getBodyContentType(): String =
        "application/json".also { this.addMarker("bodyContentType:$it") }

    override fun getBody(): ByteArray {
        val body = JSONObject()
        val md = MessageDigest.getInstance("SHA-256")
        val keyDigest = md.digest(key.toByteArray())

        var keyStr = ""
        for (b in keyDigest) {
            keyStr += String.format("%02x", b)
        }

        body.put("keyType", keyType)
        body.put("key", keyStr)
        return body.toString().toByteArray()
    }

    override fun deliverResponse(response: String) = listener.onResponse(response)

    override fun getRetryPolicy(): RetryPolicy =
        retryPolicy ?: (super.getRetryPolicy() ?: DefaultRetryPolicy())

    override fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>(super.getHeaders())
        if (faked) headers["cwa-fake"] = Math.random().toInt().toString()
        else headers["cwa-fake"] = "0"
        this.addMarker("headers:$headers")
        return headers
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
        return try {
            if (response == null) throw NullPointerException("response is null, this should never be the case")
            val strResp = String(response.data)
            val jsonObj = JSONObject(strResp)
            val registrationToken = jsonObj.getString("registrationToken")
            return Response.success(
                registrationToken,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            e.report(HTTP)
            Log.e(TAG, "invalid encoding found")
            Response.error(ParseError(e))
        } catch (e: JSONException) {
            Log.e(TAG, "invalid JSON message, probably the object parse failed")
            e.report(HTTP)
            Response.error(ParseError(e))
        }
    }
}
