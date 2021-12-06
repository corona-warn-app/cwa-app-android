package de.rki.coronawarnapp.dccticketing.core.service

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.service.processor.AccessTokenRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenInput
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenOutput
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenRequestProcessor
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
    private val accessTokenRequestProcessor: AccessTokenRequestProcessor,
    private val resultTokenRequestProcessor: ResultTokenRequestProcessor
) {

    @Throws(DccTicketingException::class)
    suspend fun requestValidationDecorator(
        url: String,
        validationServiceAllowList: Set<DccTicketingValidationServiceAllowListEntry>
    ): ValidationDecoratorRequestProcessor.ValidationDecoratorResult =
        execute("Failed to get validation decorator from $url") {
            Timber.tag(TAG).d("requestValidationDecorator(url=%s)", url)
            validationDecoratorRequestProcessor.requestValidationDecorator(
                url,
                validationServiceAllowList
            )
        }

    @Throws(DccTicketingException::class)
    suspend fun requestValidationService(
        validationService: DccTicketingService,
        validationServiceJwkSet: Set<DccJWK>,
        validationServiceAllowList: Set<DccTicketingValidationServiceAllowListEntry>
    ): ValidationServiceRequestProcessor.ValidationServiceResult =
        execute("Failed to get validation service from ${validationService.serviceEndpoint}") {
            Timber.tag(TAG).d(
                "requestValidationService(validationService=%s, validationServiceJwkSet=%s)",
                validationService,
                validationServiceJwkSet
            )
            validationServiceRequestProcessor.requestValidationService(
                validationService,
                validationServiceJwkSet,
                validationServiceAllowList
            )
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
    ): AccessTokenRequestProcessor.Output =
        execute("Failed to get access token from ${accessTokenService.serviceEndpoint}") {
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

    @Throws(DccTicketingException::class)
    suspend fun requestResultToken(resultTokenInput: ResultTokenInput): ResultTokenOutput =
        execute("Failed to get result token from ${resultTokenInput.serviceEndpoint}") {
            Timber.tag(TAG).d("requestResultToken(resultTokenInput=%s)", resultTokenInput)
            resultTokenRequestProcessor.requestResultToken(resultTokenInput)
        }

    private suspend fun <T> execute(info: String, block: suspend () -> T): T = withContext(dispatcherProvider.Default) {
        try {
            block()
        } catch (e: Exception) {
            e.reportProblem(tag = TAG, info = info)
            throw e
        }
    }

    companion object {
        private val TAG = tag<DccTicketingRequestService>()
    }
}
