package de.rki.coronawarnapp.http

import android.content.Context
import de.rki.coronawarnapp.util.ConnectivityHelper.isNetworkEnabled
import okhttp3.Interceptor
import okhttp3.Response

class OfflineCacheInterceptor(private val context: Context) : Interceptor {
    companion object {
        private const val MAX_AGE = 5
        private const val MAX_STALE_DAYS = 1
        private const val MAX_STALE = 60 * 60 * 24 * MAX_STALE_DAYS
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the request from the chain.
        var request = chain.request()
        /*
        *  Leveraging the advantage of using Kotlin,
        *  we initialize the request and change its header depending on whether
        *  the device is connected to Internet or not.
        */
        request = if (isNetworkEnabled(context)) {
            /*
             *  If there is Internet, get the cache that was stored 5 seconds ago.
             *  If the cache is older than 5 seconds, then discard it,
             *  and indicate an error in fetching the response.
             *  The 'max-age' attribute is responsible for this behavior.
             */
            request.newBuilder().header(
                "Cache-Control",
                "public, max-age=$MAX_AGE"
            ).build()
        } else {
            /*
            *  If there is no Internet, get the cache that was stored 1 days ago.
            *  If the cache is older than 1 day, then discard it,
            *  and indicate an error in fetching the response.
            *  The 'max-stale' attribute is responsible for this behavior.
            *  The 'only-if-cached' attribute indicates to not retrieve new data; fetch the cache only instead.
            */
            request.newBuilder().header(
                "Cache-Control",
                "public, only-if-cached, max-stale=$MAX_STALE"
            ).build()
        }
        return chain.proceed(request)
    }
}
