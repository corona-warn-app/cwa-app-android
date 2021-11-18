package de.rki.coronawarnapp.dccticketing.core.server

import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import timber.log.Timber

@Throws(DccTicketingException::class)
suspend fun DccTicketingServer.getServiceIdentityDocument(
    url: String,
    parserErrorCode: DccTicketingErrorCode,
    clientErrorCode: DccTicketingErrorCode,
    serverErrorCode: DccTicketingErrorCode,
    noNetworkErrorCode: DccTicketingErrorCode
) = try {
    getServiceIdentityDocument(url)
} catch (e: Exception) {
    Timber.e(e, "Getting service identity document failed")
    throw when (e) {
        is JsonParseException,
        is JsonSyntaxException -> parserErrorCode

        is CwaUnknownHostException,
        is NetworkReadTimeoutException,
        is NetworkConnectTimeoutException -> noNetworkErrorCode

        is CwaClientError -> clientErrorCode

        // Blame the server for everything else
        else -> serverErrorCode
    }.let { DccTicketingException(it, e) }
}

@Suppress("LongParameterList")
@Throws(DccTicketingException::class)
suspend fun DccTicketingServer.getAccessToken(
    url: String,
    authorization: String,
    body: AccessTokenRequest,
    clientErrorCode: DccTicketingErrorCode,
    serverErrorCode: DccTicketingErrorCode,
    noNetworkErrorCode: DccTicketingErrorCode
) = try {
    val authorizationHeader = "Bearer $authorization"
    getAccessToken(url, authorizationHeader, body)
} catch (e: Exception) {
    Timber.e(e, "Getting service identity document failed")
    throw when (e) {
        is CwaUnknownHostException,
        is NetworkReadTimeoutException,
        is NetworkConnectTimeoutException -> noNetworkErrorCode

        is CwaClientError -> clientErrorCode

        // Blame the server for everything else
        else -> serverErrorCode
    }.let { DccTicketingException(it, e) }
}
