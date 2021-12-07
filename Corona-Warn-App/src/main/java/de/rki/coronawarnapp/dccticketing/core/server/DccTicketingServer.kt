package de.rki.coronawarnapp.dccticketing.core.server

import com.google.gson.Gson
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateChecker
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException.ErrorCode
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.security.cert.Certificate
import javax.inject.Inject

@Reusable
class DccTicketingServer @Inject constructor(
    private val dccTicketingApiV1Lazy: Lazy<DccTicketingApiV1>,
    private val dispatcherProvider: DispatcherProvider,
    @BaseGson private val gson: Gson,
    private val serverCertificateChecker: DccTicketingServerCertificateChecker
) {

    private val dccTicketingApiV1: DccTicketingApiV1
        get() = dccTicketingApiV1Lazy.get()

    @Throws(DccTicketingServerException::class)
    suspend fun getServiceIdentityDocument(
        url: String
    ): DccTicketingServiceIdentityDocument = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("getServiceIdentityDocument(url=%s)", url)
        get(url).parse()
    }

    @Throws(DccTicketingServerException::class, DccTicketingServerCertificateCheckException::class)
    suspend fun getServiceIdentityDocumentAndValidateServerCert(
        url: String,
        allowList: Set<DccTicketingValidationServiceAllowListEntry>
    ): DccTicketingServiceIdentityDocument = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("getServiceIdentityDocument(url=%s, allowlist=%s)", url, allowList)
        get(url).run {
            validateAgainstAllowlist(allowList = allowList)
            parse()
        }
    }

    private suspend fun get(url: String): Response<ResponseBody> = try {
        dccTicketingApiV1.getUrl(url)
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

    private inline fun <reified T> Response<ResponseBody>.parse(): T = try {
        Timber.tag(TAG).d("Parsing response=%s", this)
        body()!!.charStream().use { gson.fromJson(it) }
    } catch (e: Exception) {
        Timber.e(e, "Parsing failed")
        throw DccTicketingServerException(errorCode = ErrorCode.PARSE_ERR, cause = e)
    }

    private fun Response<ResponseBody>.validateAgainstJwkSet(jwkSet: Set<DccJWK>) {
        Timber.tag(TAG).d("Validating response with jwk set=%s", jwkSet)
        serverCertificateChecker.checkCertificate(serverCertificateChain, jwkSet)
    }

    private fun Response<ResponseBody>.validateAgainstAllowlist(
        allowList: Set<DccTicketingValidationServiceAllowListEntry>
    ) {
        Timber.tag(TAG).d("Validating response with against allow list=%s", allowList)
        serverCertificateChecker.checkCertificate(
            hostname = hostname,
            certificateChain = serverCertificateChain,
            allowList = allowList
        )
    }

    private val Response<ResponseBody>.serverCertificateChain: List<Certificate>
        get() = raw().handshake?.peerCertificates ?: emptyList()

    private val Response<ResponseBody>.hostname: String
        get() = raw().request.url.host

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getAccessToken(
        url: String,
        authorizationHeader: String,
        requestBody: AccessTokenRequest,
        jwkSet: Set<DccJWK>
    ): AccessTokenResponse =
        withContext(dispatcherProvider.IO) {
            Timber.d("getAccessToken(url=%s)", url)
            val response = dccTicketingApiV1.getAccessToken(url, authorizationHeader, requestBody)
            response.validateAgainstJwkSet(jwkSet)

            val jwtToken: String = response.body()?.string()!!
            val iv = response.headers()["x-nonce"]!!
            AccessTokenResponse(jwtToken, iv)
        }

    suspend fun getResultTokenAndValidate(
        url: String,
        authorizationHeader: String,
        requestBody: ResultTokenRequest,
        allowList: Set<DccTicketingValidationServiceAllowListEntry>
    ): Response<ResponseBody> =
        withContext(dispatcherProvider.IO) {
            Timber.d("getResultToken(url=%s)", url)
            dccTicketingApiV1.getResultToken(
                url,
                authorizationHeader,
                requestBody
            ).also { it.validateAgainstAllowlist(allowList = allowList) }
        }

    companion object {
        private val TAG = tag<DccTicketingServer>()
    }
}

data class AccessTokenResponse(val jwt: String, val iv: String)
