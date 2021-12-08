package de.rki.coronawarnapp.dccticketing.ui.consent.one

import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.encryption.ec.EcKeyGenerator
import timber.log.Timber
import javax.inject.Inject

class DccTicketingConsentOneProcessor @Inject constructor(
    private val dccTicketingRequestService: DccTicketingRequestService,
    private val ecKeyGenerator: EcKeyGenerator
) {

    suspend fun updateTransactionContext(ctx: DccTicketingTransactionContext): DccTicketingTransactionContext {
        Timber.tag(TAG).d("updateTransactionContext(ctx=%s)", ctx)
        return ctx
            .requestServiceIdentityDocumentOfValidationService()
            .generateECKeyPair()
            .requestAccessToken()
            .also { Timber.tag(TAG).d("Updated %s", it) }
    }

    private suspend fun DccTicketingTransactionContext.requestServiceIdentityDocumentOfValidationService():
        DccTicketingTransactionContext {
            Timber.tag(TAG).d("requestServiceIdentityDocumentOfValidationService")

            requireNotNull(validationService) { "ctx.validationService must not be null" }
            requireNotNull(validationServiceJwkSet) { "ctx.validationServiceJwkSet must not be null" }
            requireNotNull(allowlist) { "ctx.allowlist must not be null" }

            val document = dccTicketingRequestService.requestValidationService(
                validationService,
                validationServiceJwkSet,
                allowlist
            )

            return copy(
                validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC =
                document.validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC,
                validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM =
                document.validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM,
                validationServiceSignKeyJwkSet =
                document.validationServiceSignKeyJwkSet
            )
        }

    private fun DccTicketingTransactionContext.generateECKeyPair(): DccTicketingTransactionContext {
        Timber.tag(TAG).d("generateECKeyPair()")

        val ecKeyPair = ecKeyGenerator.generateECKeyPair()

        return copy(
            ecPublicKey = ecKeyPair.publicKey,
            ecPrivateKey = ecKeyPair.privateKey,
            ecPublicKeyBase64 = ecKeyPair.publicKeyBase64
        )
    }

    private suspend fun DccTicketingTransactionContext.requestAccessToken(): DccTicketingTransactionContext {
        Timber.tag(TAG).d("requestAccessToken()")

        requireNotNull(accessTokenService) { "ctx.accessTokenService must not be null" }
        requireNotNull(accessTokenServiceJwkSet) { "ctx.accessTokenServiceJwkSet must not be null" }
        requireNotNull(accessTokenSignJwkSet) { "ctx.accessTokenSignJwkSet must not be null" }
        requireNotNull(validationService) { "ctx.validationService must not be null" }
        requireNotNull(ecPublicKeyBase64) { "ctx.ecPublicKeyBase64 must not be null" }

        val accessToken = dccTicketingRequestService.requestAccessToken(
            accessTokenService = accessTokenService,
            accessTokenServiceJwkSet = accessTokenServiceJwkSet,
            accessTokenSignJwkSet = accessTokenSignJwkSet,
            authorization = initializationData.token,
            validationService = validationService,
            publicKeyBase64 = ecPublicKeyBase64
        )

        return copy(
            accessToken = accessToken.accessToken,
            accessTokenPayload = accessToken.accessTokenPayload,
            nonceBase64 = accessToken.nonceBase64
        )
    }

    companion object {
        private val TAG = tag<DccTicketingConsentOneProcessor>()
    }
}
