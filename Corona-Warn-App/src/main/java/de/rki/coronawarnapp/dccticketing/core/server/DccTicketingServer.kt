package de.rki.coronawarnapp.dccticketing.core.server

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException.ErrorCode
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.exception.CwaWebSecurityException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingServer @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val apiProvider: DccTicketingApiProvider
) {

    @Throws(DccTicketingServerException::class)
    suspend fun getServiceIdentityDocument(
        url: String
    ): Response<ResponseBody> = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("getServiceIdentityDocument(url=%s)", url)
        get(url)
    }

    private suspend fun get(url: String): Response<ResponseBody> = try {
        apiProvider.getDccTicketingApiV1().getUrl(url)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Get request failed.")
        throw when (e) {
            is CwaUnknownHostException,
            is NetworkReadTimeoutException,
            is NetworkConnectTimeoutException -> ErrorCode.NO_NETWORK
            is CwaClientError -> ErrorCode.CLIENT_ERR
            else -> ErrorCode.SERVER_ERR
        }.let { DccTicketingServerException(errorCode = it, cause = e) }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getAccessToken(
        url: String,
        authorizationHeader: String,
        requestBody: AccessTokenRequest,
        jwkSet: Set<DccJWK>
    ): AccessTokenResponse =
        withContext(dispatcherProvider.IO) {
            try {
                Timber.d("getAccessToken(url=%s)", url)
                val response = apiProvider.getDccTicketingApiV1(
                    url,
                    jwkSet
                ).getAccessToken(
                    url,
                    authorizationHeader,
                    requestBody
                )
                val jwtToken: String = response.body()?.string()!!
                val iv = response.headers()["x-nonce"]!!
                AccessTokenResponse(jwtToken, iv)
            } catch (e: Exception) {
                throw when {
                    e is CwaWebSecurityException && e.cause is javax.net.ssl.SSLPeerUnverifiedException ->
                            DccTicketingServerCertificateCheckException(CERT_PIN_MISMATCH)
                    else -> e
                }
            }
        }

    suspend fun getResultToken(
        url: String,
        authorizationHeader: String,
        requestBody: ResultTokenRequest
    ): Response<ResponseBody> =
        withContext(dispatcherProvider.IO) {
            Timber.d("getResultToken(url=%s)", url)
            apiProvider.getDccTicketingApiV1().getResultToken(
                url,
                authorizationHeader,
                requestBody
            )
        }

    companion object {
        private val TAG = tag<DccTicketingServer>()
    }
}

data class AccessTokenResponse(val jwt: String, val iv: String)
