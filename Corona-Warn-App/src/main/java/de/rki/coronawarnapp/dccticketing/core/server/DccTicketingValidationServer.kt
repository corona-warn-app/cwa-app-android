package de.rki.coronawarnapp.dccticketing.core.server

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingValidationServer @Inject constructor(
    private val dccTicketingValidationApiV1Lazy: Lazy<DccTicketingValidationApiV1>,
    private val dispatcherProvider: DispatcherProvider
) {

    private val dccTicketingValidationApiV1: DccTicketingValidationApiV1
        get() = dccTicketingValidationApiV1Lazy.get()

    suspend fun requestServiceIdentityDocumentValidationDecorator(serviceEndpoint: String): Unit =
        withContext(dispatcherProvider.IO) {
            Timber.d("requestServiceIdentityDocumentValidationDecorator(serviceEndpoint=%s)", serviceEndpoint)

            /*
            try {

            } catch (e: Exception) {
                throw when(e) {
                    is CwaUnknownHostException,
                    is NetworkReadTimeoutException,
                    is NetworkConnectTimeoutException ->
                    is CwaClientError ->
                    is CwaServerError ->
                }
            }

             */

        }

    suspend fun requestServiceIdentityDocumentValidationService(url: String): Unit =
        withContext(dispatcherProvider.IO) {
            dccTicketingValidationApiV1.getServiceIdentityDocument(url)
        }
}
