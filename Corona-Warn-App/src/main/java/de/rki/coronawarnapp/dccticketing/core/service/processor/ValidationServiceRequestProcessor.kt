package de.rki.coronawarnapp.dccticketing.core.service.processor

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ValidationServiceRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer
) {

    private val regexRSAOAEPWithSHA256AESCBC = """/ValidationServiceEncScheme-RSAOAEPWithSHA256AESCBC${'$'}/"""
        .toRegex()

    private val regexRSAOAEPWithSHA256AESGCM =
        """/ValidationServiceEncScheme-RSAOAEPWithSHA256AESGCM${'$'}/"""
            .toRegex()

    @Throws(DccTicketingException::class)
    suspend fun requestValidationService(
        validationService: DccTicketingService,
        validationServiceJwkSet: Set<DccJWK>
    ): Output {
        Timber.d(
            "requestValidationService(validationService=%s, validationServiceJwkSet=%s)",
            validationService,
            validationServiceJwkSet
        )

        // 1. Call Service Identity Document
        val serviceIdentityDocument = getServiceIdentityDocument(
            url = validationService.serviceEndpoint,
            jwkSet = validationServiceJwkSet
        )

        // 2. Find verificationMethodsForRSAOAEPWithSHA256AESCBC
        val verificationMethodsForRSAOAEPWithSHA256AESCBC = serviceIdentityDocument
            .findVerificationMethods(forRegex = regexRSAOAEPWithSHA256AESCBC)

        // 3. Find validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = serviceIdentityDocument
            .findValidationServiceEncKeyJwkSet(verificationMethodIds = verificationMethodsForRSAOAEPWithSHA256AESCBC)
            .also { Timber.d("validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC=%s", it) }

        // 4. Find verificationMethodsForRSAOAEPWithSHA256AESGCM
        val verificationMethodsForRSAOAEPWithSHA256AESGCM = serviceIdentityDocument
            .findVerificationMethods(forRegex = regexRSAOAEPWithSHA256AESGCM)

        // 5. Find validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = serviceIdentityDocument
            .findValidationServiceEncKeyJwkSet(verificationMethodIds = verificationMethodsForRSAOAEPWithSHA256AESGCM)
            .also { Timber.d("validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM=%s", it) }

        // 6. Check encryption key
        if (validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC.isEmpty() && validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM.isEmpty()) {
            Timber.d("Didn't find encryption keys, aborting")
            throw DccTicketingException(errorCode = DccTicketingErrorCode.VS_ID_NO_ENC_KEY)
        }

        // 7. Find validationServiceSignKeyJwkSet
        val validationServiceSignKeyJwkSet = serviceIdentityDocument
            .findJwkSet(jwkSetType = JwkSetType.ValidationServiceSignKeyJwkSet)

        return Output(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC,
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM,
            validationServiceSignKeyJwkSet = validationServiceSignKeyJwkSet
        ).also { Timber.d("Returning output=%s", it) }
    }

    private suspend fun getServiceIdentityDocument(
        url: String,
        jwkSet: Set<DccJWK>
    ): DccTicketingServiceIdentityDocument = try {
        Timber.d("getServiceIdentityDocument(url=%s, jwkSet=%s)", url, jwkSet)
        dccTicketingServer.getServiceIdentityDocumentAndValidateServerCert(url = url, jwkSet = jwkSet)
    } catch (e: DccTicketingServerException) {
        Timber.e(e, "Getting ServiceIdentityDocument failed")
        throw when (e.errorCode) {
            DccTicketingServerException.ErrorCode.PARSE_ERR -> DccTicketingErrorCode.VS_ID_PARSE_ERR
            DccTicketingServerException.ErrorCode.SERVER_ERR -> DccTicketingErrorCode.VS_ID_SERVER_ERR
            DccTicketingServerException.ErrorCode.CLIENT_ERR -> DccTicketingErrorCode.VS_ID_CLIENT_ERR
            DccTicketingServerException.ErrorCode.NO_NETWORK -> DccTicketingErrorCode.VS_ID_NO_NETWORK
        }.let { DccTicketingException(errorCode = it, cause = e) }
    } catch (e: DccTicketingServerCertificateCheckException) {
        Timber.e(e, "Getting ServiceIdentityDocument failed")
        throw when (e.errorCode) {
            DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID -> DccTicketingErrorCode.VS_ID_CERT_PIN_NO_JWK_FOR_KID
            DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH -> DccTicketingErrorCode.VS_ID_CERT_PIN_MISMATCH
        }.let { DccTicketingException(errorCode = it, cause = e) }
    }

    private fun DccTicketingServiceIdentityDocument.findVerificationMethods(forRegex: Regex): Set<String> {
        Timber.d("findVerificationMethods(forRegex=%s)", forRegex)
        return verificationMethod
            .firstOrNull { it.id.matches(forRegex) }
            ?.verificationMethods
            ?.toSet() ?: emptySet()
    }

    private fun DccTicketingServiceIdentityDocument.findValidationServiceEncKeyJwkSet(
        verificationMethodIds: Set<String>
    ): Set<DccJWK> {
        Timber.d("findValidationServiceEncKeyJwkSet=%s", verificationMethodIds)
        return verificationMethod
            .filter { verificationMethodIds.contains(it.id) }
            .mapNotNull { it.publicKeyJwk }
            .toSet()
    }

    data class Output(
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC: Set<DccJWK>,
        val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM: Set<DccJWK>,
        val validationServiceSignKeyJwkSet: Set<DccJWK>
    )
}
