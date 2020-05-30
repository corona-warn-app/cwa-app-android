package de.rki.coronawarnapp.http

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import de.rki.coronawarnapp.CoronaWarnApplication

/**
 * Request queue holder used to reference a singleton of a Volley request queue for simple web requests.
 * For more complex queries we use the os-owned DownloadManager.
 */
object RequestQueueHolder {

    /**
     * lazily initialized singleton reference to a request queue.
     */
    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(CoronaWarnApplication.getAppContext())
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
