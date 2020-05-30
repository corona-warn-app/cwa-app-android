package de.rki.coronawarnapp.http.request

import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import de.rki.coronawarnapp.storage.FileStorageHelper
import java.io.File
import java.util.UUID

/**
 * Request Class used for querying ZIP files containing the key files
 *
 * @property requestTag a given request tag to add as tag
 * @property headers given header values
 * @property listener given listener to call on response
 *
 * @param url given request URL
 * @param errorListener given listener to call on error
 */
class KeyFileRequest(
    url: String,
    private val requestTag: Any?,
    private val headers: MutableMap<String, String>?,
    private val listener: Response.Listener<File>,
    errorListener: Response.ErrorListener
) : Request<File>(Method.GET, url, errorListener) {

    override fun getTag(): Any = requestTag ?: super.getTag().also { this.addMarker("tag:$it") }
    override fun deliverResponse(response: File) = listener.onResponse(response)
    override fun getHeaders(): MutableMap<String, String> = headers ?: super.getHeaders()

    override fun parseNetworkResponse(response: NetworkResponse): Response<File> {
        val fileName = "${UUID.nameUUIDFromBytes(url.toByteArray())}.zip"
        Log.v(tag.toString(), "$url results in file name $fileName")
        val file = File(FileStorageHelper.keyExportDirectory, fileName)
        file.outputStream().use {
            response.data.inputStream().copyTo(it, DEFAULT_BUFFER_SIZE)
        }
        return Response.success(file, HttpHeaderParser.parseCacheHeaders(response))
    }
}
