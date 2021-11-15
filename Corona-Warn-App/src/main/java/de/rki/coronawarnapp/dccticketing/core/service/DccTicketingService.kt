package de.rki.coronawarnapp.dccticketing.core.service

import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingService @Inject constructor(
    private val dccTicketingServer: DccTicketingServer,
    private val dispatcherProvider: DispatcherProvider,
) {

    @Throws(DccTicketingException::class)
    suspend fun requestServiceIdentityDocumentValidationDecorator() = withContext(dispatcherProvider.Default){

    }

    @Throws(DccTicketingException::class)
    suspend fun requestServiceIdentityDocumentValidationService() = withContext(dispatcherProvider.Default) {

    }

    private suspend fun getServiceIdentityDocument(
        url: String,
        parserErrorCode: DccTicketingErrorCode,
        clientErrorCode: DccTicketingErrorCode,
        serverErrorCode: DccTicketingErrorCode,
        noNetworkErrorCode: DccTicketingErrorCode
    ) = try {
        dccTicketingServer.getServiceIdentityDocument(url)
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
}

private typealias DccTicketingErrorCode = DccTicketingException.ErrorCode
