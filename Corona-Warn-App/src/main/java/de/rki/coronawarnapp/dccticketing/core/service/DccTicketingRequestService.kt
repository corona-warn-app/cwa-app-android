package de.rki.coronawarnapp.dccticketing.core.service

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.service.processor.AccessTokenRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationDecoratorRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationServiceRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingRequestService @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val validationDecoratorRequestProcessor: ValidationDecoratorRequestProcessor,
    private val validationServiceRequestProcessor: ValidationServiceRequestProcessor,
    private val accessTokenRequestProcessor: AccessTokenRequestProcessor
) {

    @Throws(DccTicketingException::class)
    suspend fun requestValidationDecorator(
        url: String
    ): ValidationDecoratorRequestProcessor.Output = withContext(dispatcherProvider.Default) {
        Timber.d("requestValidationDecorator(url=%s)", url)
        validationDecoratorRequestProcessor.requestValidationDecorator(url)
    }

    @Throws(DccTicketingException::class)
    suspend fun requestValidationService(
        validationService: DccTicketingService,
        validationServiceJwkSet: Set<DccJWK>
    ): ValidationServiceRequestProcessor.Output = withContext(dispatcherProvider.Default) {
        Timber.d(
            "requestValidationService(validationService=%s, validationServiceJwkSet=%s)",
            validationService,
            validationServiceJwkSet
        )
        validationServiceRequestProcessor.requestValidationService(validationService, validationServiceJwkSet)
    }

    @Throws(DccTicketingException::class)
    suspend fun requestAccessToken(): AccessTokenRequestProcessor.Output = withContext(dispatcherProvider.Default) {
        // TODO: Add input
        Timber.d("requestAccessToken()")
        accessTokenRequestProcessor.requestAccessToken()
    }
}
