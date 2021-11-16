package de.rki.coronawarnapp.dccticketing.core.server

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.DccTicketing
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateChecker
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException.ErrorCode
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingServer @Inject constructor(
    private val dccTicketingApiV1Lazy: Lazy<DccTicketingApiV1>,
    private val dispatcherProvider: DispatcherProvider,
    @DccTicketing private val client: OkHttpClient,
    @DccTicketing private val gson: Gson,
    private val serverCertificateChecker: DccTicketingServerCertificateChecker
) {

    private val dccTicketingApiV1: DccTicketingApiV1
        get() = dccTicketingApiV1Lazy.get()

    @Throws(DccTicketingServerException::class)
    suspend fun getServiceIdentityDocument(
        url: String
    ): DccTicketingServiceIdentityDocument = withContext(dispatcherProvider.IO) {
        Timber.d("getServiceIdentityDocument(url=%s)", url)
        get(url).parse()
    }

    @Throws(DccTicketingServerException::class, DccTicketingServerCertificateCheckException::class)
    suspend fun getServiceIdentityDocumentAndValidateServerCert(
        url: String,
        jwkSet: Set<DccJWK>
    ): DccTicketingServiceIdentityDocument = withContext(dispatcherProvider.IO) {
        Timber.d("getServiceIdentityDocument(url=%s)", url)
        get(url).run {
            validate(jwkSet = jwkSet)
            parse()
        }
    }

    private fun get(url: String): Response = try {
        Timber.d("Get %s", url)
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute()
    } catch (e: Exception) {
        Timber.e(e, "Get request failed.")
        throw when (e) {
            is CwaUnknownHostException,
            is NetworkReadTimeoutException,
            is NetworkConnectTimeoutException -> ErrorCode.NO_NETWORK
            is CwaClientError -> ErrorCode.CLIENT_ERR
            else -> ErrorCode.SERVER_ERR
        }.let { DccTicketingServerException(errorCode = it, cause = e) }
    }

    private fun <T> Response.parse(): T = try {
        Timber.d("Parsing response=%s", this)
        body!!.charStream().use { gson.fromJson(it, object : TypeToken<T>() {}.type) }
    } catch (e: Exception) {
        Timber.e(e, "Parsing failed")
        throw DccTicketingServerException(errorCode = ErrorCode.PARSE_ERR, cause = e)
    }

    private fun Response.validate(jwkSet: Set<DccJWK>) {
        Timber.d("Validating response=%s")
        val certificateChain = handshake?.peerCertificates ?: emptyList()
        serverCertificateChecker.checkCertificate(certificateChain, jwkSet)
    }
}
