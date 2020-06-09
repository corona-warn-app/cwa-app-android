package de.rki.coronawarnapp.http.interceptor

import de.rki.coronawarnapp.exception.CwaWebSecurityException
import okhttp3.Interceptor
import okhttp3.Response
import javax.net.ssl.SSLException

class WebSecurityVerificationInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (e: SSLException) {
            throw CwaWebSecurityException(e)
        }
    }
}
