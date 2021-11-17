package de.rki.coronawarnapp.dccticketing.core.service.processor

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ValidationDecoratorRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer
) {

    @Throws(DccTicketingException::class)
    suspend fun requestValidationDecorator(url: String): ValidationDecoratorResult {
        Timber.tag(TAG).d("requestServiceIdentityDocumentValidationDecorator(url=%s)", url)

        // Call Service Identity Document
        val serviceIdentityDocument = getServiceIdentityDocument(url = url)

        // Verify JWKs
        if (serviceIdentityDocument.verificationMethod
            .any { it.publicKeyJwk != null && it.publicKeyJwk.x5c.isEmpty() }
        ) {
            Timber.tag(TAG).d("x5c does not contain at least one element")
            throw DccTicketingException(errorCode = DccTicketingException.ErrorCode.VD_ID_EMPTY_X5C)
        }

        // Find accessTokenService
        val accessTokenService = serviceIdentityDocument.findService(serviceType = ServiceType.AccessTokenService)

        // Find accessTokenSignJwkSet
        val accessTokenSignJwkSet = serviceIdentityDocument.findJwkSet(jwkSetType = JwkSetType.AccessTokenSignJwkSet)

        // Find accessTokenServiceJwkSet
        val accessTokenServiceJwkSet = serviceIdentityDocument
            .findJwkSet(jwkSetType = JwkSetType.AccessTokenServiceJwkSet)

        // Find validationService
        val validationService = serviceIdentityDocument.findService(serviceType = ServiceType.ValidationService)

        // Find validationServiceJwkSet
        val validationServiceJwkSet = serviceIdentityDocument
            .findJwkSet(jwkSetType = JwkSetType.ValidationServiceJwkSet)

        return ValidationDecoratorResult(
            accessTokenService = accessTokenService,
            accessTokenServiceJwkSet = accessTokenServiceJwkSet,
            accessTokenSignJwkSet = accessTokenSignJwkSet,
            validationService = validationService,
            validationServiceJwkSet = validationServiceJwkSet
        ).also { Timber.tag(TAG).d("Returning ValidationDecoratorResult=%s", it) }
    }

    private suspend fun getServiceIdentityDocument(url: String): DccTicketingServiceIdentityDocument = try {
        Timber.d("getServiceIdentityDocument(url=%s)", url)
        dccTicketingServer.getServiceIdentityDocument(url = url)
    } catch (e: DccTicketingServerException) {
        Timber.tag(TAG).e(e, "Getting ServiceIdentityDocument failed")
        throw when (e.errorCode) {
            DccTicketingServerException.ErrorCode.PARSE_ERR -> DccTicketingErrorCode.VD_ID_PARSE_ERR
            DccTicketingServerException.ErrorCode.SERVER_ERR -> DccTicketingErrorCode.VD_ID_SERVER_ERR
            DccTicketingServerException.ErrorCode.CLIENT_ERR -> DccTicketingErrorCode.VD_ID_CLIENT_ERR
            DccTicketingServerException.ErrorCode.NO_NETWORK -> DccTicketingErrorCode.VD_ID_NO_NETWORK
        }.let { DccTicketingException(errorCode = it, cause = e) }
    }

    private fun DccTicketingServiceIdentityDocument.findService(serviceType: ServiceType): DccTicketingService {
        Timber.tag(TAG).d(
            message = "findService(serviceType=%s, notFoundErrorCode=%s)",
            serviceType.type,
            serviceType.notFoundErrorCode
        )

        val foundService = service.firstOrNull { it.type == serviceType.type }
        if (foundService == null) {
            Timber.tag(TAG).d("No matching entries for %s, aborting", serviceType)
            throw DccTicketingException(errorCode = serviceType.notFoundErrorCode)
        }
        return foundService.also { Timber.d("Found %s=%s", serviceType.type, foundService) }
    }

    data class ValidationDecoratorResult(
        val accessTokenService: DccTicketingService,
        val accessTokenServiceJwkSet: Set<DccJWK>,
        val accessTokenSignJwkSet: Set<DccJWK>,
        val validationService: DccTicketingService,
        val validationServiceJwkSet: Set<DccJWK>
    )

    companion object {
        private val TAG = tag<ValidationDecoratorRequestProcessor>()
    }
}

private enum class ServiceType(
    val type: String,
    val notFoundErrorCode: DccTicketingErrorCode
) {
    AccessTokenService(
        type = "AccessTokenService",
        notFoundErrorCode = DccTicketingErrorCode.VD_ID_NO_ATS
    ),

    ValidationService(
        type = "ValidationService",
        notFoundErrorCode = DccTicketingErrorCode.VD_ID_NO_VS
    )
}
