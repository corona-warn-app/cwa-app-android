package de.rki.coronawarnapp.dccticketing.core.service.processor

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException.ErrorCode.*
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
class ValidationServiceRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer
) {

    private val regexRSAOAEPWithSHA256AESCBC = """ValidationServiceEncScheme-RSAOAEPWithSHA256AESCBC${'$'}"""
        .toRegex()

    private val regexRSAOAEPWithSHA256AESGCM =
        """ValidationServiceEncScheme-RSAOAEPWithSHA256AESGCM${'$'}"""
            .toRegex()

    @Throws(DccTicketingException::class)
    suspend fun requestValidationService(
        validationService: DccTicketingService,
        validationServiceJwkSet: Set<DccJWK>
    ): ValidationServiceResult {
        Timber.tag(TAG).d(
            "requestValidationService(validationService=%s, validationServiceJwkSet=%s)",
            validationService,
            validationServiceJwkSet
        )

        // 1. Call Service Identity Document
        val serviceIdentityDocument = getServiceIdentityDocument(
            url = validationService.serviceEndpoint,
            jwkSet = validationServiceJwkSet
        )

        // 2. Verify JWKs
        serviceIdentityDocument.verifyJwks(emptyX5cErrorCode = DccTicketingErrorCode.VS_ID_EMPTY_X5C)

        // 3. Find verificationMethodsForRSAOAEPWithSHA256AESCBC
        val verificationMethodsForRSAOAEPWithSHA256AESCBC = serviceIdentityDocument
            .findVerificationMethods(forRegex = regexRSAOAEPWithSHA256AESCBC)

        // 4. Find validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = serviceIdentityDocument
            .findValidationServiceEncKeyJwkSet(verificationMethodIds = verificationMethodsForRSAOAEPWithSHA256AESCBC)
            .also { Timber.tag(TAG).d("validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC=%s", it) }

        // 5. Find verificationMethodsForRSAOAEPWithSHA256AESGCM
        val verificationMethodsForRSAOAEPWithSHA256AESGCM = serviceIdentityDocument
            .findVerificationMethods(forRegex = regexRSAOAEPWithSHA256AESGCM)

        // 6. Find validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = serviceIdentityDocument
            .findValidationServiceEncKeyJwkSet(verificationMethodIds = verificationMethodsForRSAOAEPWithSHA256AESGCM)
            .also { Timber.tag(TAG).d("validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM=%s", it) }

        // 7. Check encryption key
        if (
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC.isEmpty() &&
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM.isEmpty()
        ) {
            Timber.tag(TAG).d("Didn't find encryption keys, aborting")
            throw DccTicketingException(errorCode = DccTicketingErrorCode.VS_ID_NO_ENC_KEY)
        }

        // 8. Find validationServiceSignKeyJwkSet
        val validationServiceSignKeyJwkSet = serviceIdentityDocument
            .findJwkSet(jwkSetType = JwkSetType.ValidationServiceSignKeyJwkSet)

        return ValidationServiceResult(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC =
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC,
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM =
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM,
            validationServiceSignKeyJwkSet = validationServiceSignKeyJwkSet
        ).also { Timber.tag(TAG).d("Returning %s", it) }
    }

    private suspend fun getServiceIdentityDocument(
        url: String,
        jwkSet: Set<DccJWK>
    ): DccTicketingServiceIdentityDocument = try {
        Timber.tag(TAG).d("getServiceIdentityDocument(url=%s, jwkSet=%s)", url, jwkSet)
        dccTicketingServer.getServiceIdentityDocumentAndValidateServerCert(url = url, jwkSet = jwkSet)
    } catch (e: DccTicketingServerException) {
        Timber.tag(TAG).e(e, "Getting ServiceIdentityDocument failed")
        throw when (e.errorCode) {
            DccTicketingServerException.ErrorCode.PARSE_ERR -> DccTicketingErrorCode.VS_ID_PARSE_ERR
            DccTicketingServerException.ErrorCode.SERVER_ERR -> DccTicketingErrorCode.VS_ID_SERVER_ERR
            DccTicketingServerException.ErrorCode.CLIENT_ERR -> DccTicketingErrorCode.VS_ID_CLIENT_ERR
            DccTicketingServerException.ErrorCode.NO_NETWORK -> DccTicketingErrorCode.VS_ID_NO_NETWORK
        }.let { DccTicketingException(errorCode = it, cause = e) }
    } catch (e: DccTicketingServerCertificateCheckException) {
        Timber.tag(TAG).e(e, "Getting ServiceIdentityDocument failed")
        throw when (e.errorCode) {
            CERT_PIN_HOST_MISMATCH,
            CERT_PIN_NO_JWK_FOR_KID ->
                DccTicketingErrorCode.VS_ID_CERT_PIN_HOST_MISMATCH
            CERT_PIN_MISMATCH ->
                DccTicketingErrorCode.VS_ID_CERT_PIN_MISMATCH
        }.let { DccTicketingException(errorCode = it, cause = e) }
    }

    private fun DccTicketingServiceIdentityDocument.findVerificationMethods(forRegex: Regex): Set<String> {
        Timber.tag(TAG).d("findVerificationMethods(forRegex=%s)", forRegex)
        return verificationMethod
            .firstOrNull { forRegex.containsMatchIn(it.id) }
            ?.verificationMethods
            ?.toSet() ?: emptySet()
    }

    private fun DccTicketingServiceIdentityDocument.findValidationServiceEncKeyJwkSet(
        verificationMethodIds: Set<String>
    ): Set<DccJWK> {
        Timber.tag(TAG).d("findValidationServiceEncKeyJwkSet=%s", verificationMethodIds)
        return verificationMethod
            .filter { verificationMethodIds.contains(it.id) }
            .mapNotNull { it.publicKeyJwk }
            .toSet()
    }

    data class ValidationServiceResult(
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC: Set<DccJWK>,
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM: Set<DccJWK>,
        val validationServiceSignKeyJwkSet: Set<DccJWK>
    )

    companion object {
        private val TAG = tag<ValidationServiceRequestProcessor>()
    }
}
