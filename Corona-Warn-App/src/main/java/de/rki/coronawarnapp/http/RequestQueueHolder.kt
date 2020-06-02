package de.rki.coronawarnapp.http

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.security.PinnedTLSSocketFactory
import de.rki.coronawarnapp.util.security.SecurityHelper

/**
 * Request queue holder used to reference a singleton of a Volley request queue for simple web requests.
 * For more complex queries we use the os-owned DownloadManager.
 */
object RequestQueueHolder {
    private const val PUBLIC_KEY = "volley_pinned_certs"

    /**
     * lazily initialized singleton reference to a request queue.
     */
    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        SecurityHelper.getPinnedWebStack(CoronaWarnApplication.getAppContext())
    }

    /**
     * Adds a request to the queue.
     *
     * @param T return type of the request
     * @param req a given request
     */
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}
