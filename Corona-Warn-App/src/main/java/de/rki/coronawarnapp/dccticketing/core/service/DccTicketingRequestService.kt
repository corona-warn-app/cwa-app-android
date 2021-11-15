package de.rki.coronawarnapp.dccticketing.core.service

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.getServiceIdentityDocument
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingService @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val validationDecoratorRequest: ValidationDecoratorRequest
) {

    @Throws(DccTicketingException::class)
    suspend fun requestServiceIdentityDocumentValidationDecorator(
        url: String
    ): ValidationDecoratorRequest.Output = withContext(dispatcherProvider.Default) {
        Timber.d("requestServiceIdentityDocumentValidationDecorator(url=%s)", url)
        validationDecoratorRequest.requestValidationDecorator(url)
    }

    @Throws(DccTicketingException::class)
    suspend fun requestServiceIdentityDocumentValidationService() = withContext(dispatcherProvider.Default) {

    }
}


