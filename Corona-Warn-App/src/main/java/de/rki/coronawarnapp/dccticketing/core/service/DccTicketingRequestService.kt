package de.rki.coronawarnapp.dccticketing.core.service

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.service.processor.AccessTokenRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationDecoratorRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationServiceRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.tag
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
    ): ValidationDecoratorRequestProcessor.ValidationDecoratorResult = withContext(dispatcherProvider.Default) {
        Timber.tag(TAG).d("requestValidationDecorator(url=%s)", url)
        validationDecoratorRequestProcessor.requestValidationDecorator(url)
    }

    @Throws(DccTicketingException::class)
    suspend fun requestValidationService(
        validationService: DccTicketingService,
        validationServiceJwkSet: Set<DccJWK>
    ): ValidationServiceRequestProcessor.ValidationServiceResult = withContext(dispatcherProvider.Default) {
        Timber.tag(TAG).d(
            "requestValidationService(validationService=%s, validationServiceJwkSet=%s)",
            validationService,
            validationServiceJwkSet
        )
        validationServiceRequestProcessor.requestValidationService(validationService, validationServiceJwkSet)
    }

    @Suppress("LongParameterList")
    @Throws(DccTicketingException::class)
    suspend fun requestAccessToken(
        accessTokenService: DccTicketingService,
        accessTokenServiceJwkSet: Set<DccJWK>,
        accessTokenSignJwkSet: Set<DccJWK>,
        validationService: DccTicketingService,
        publicKeyBase64: String,
        authorization: String
    ): AccessTokenRequestProcessor.Output = withContext(dispatcherProvider.Default) {
        Timber.tag(TAG).d("requestAccessToken()")
        accessTokenRequestProcessor.requestAccessToken(
            accessTokenService,
            accessTokenServiceJwkSet,
            accessTokenSignJwkSet,
            validationService,
            publicKeyBase64,
            authorization
        )
    }

    companion object {
        private val TAG = tag<DccTicketingRequestService>()
    }
}
