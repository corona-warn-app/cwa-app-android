package de.rki.coronawarnapp.dccticketing.core.submission

import de.rki.coronawarnapp.dccticketing.core.common.DccJWKConverter
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.VS_ID_CLIENT_ERR
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.VS_ID_NO_ENC_KEY
import de.rki.coronawarnapp.dccticketing.core.security.DccTicketingSecurityTool
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenInput
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import timber.log.Timber
import javax.inject.Inject

class DccTicketingSubmissionHandler @Inject constructor(
    private val securityTool: DccTicketingSecurityTool,
    private val converter: DccJWKConverter,
    private val processor: ResultTokenRequestProcessor
) {

    suspend fun submitDcc(transactionContext: DccTicketingTransactionContext): DccTicketingTransactionContext {
        try {
            val encryptionParameters = transactionContext.getEncryptionParameters()
            val signedOutput =
                securityTool.encryptAndSign(transactionContext.getSecurityToolInput(encryptionParameters))

            val context = transactionContext.copy(
                encryptedDCCBase64 = signedOutput.encryptedDCCBase64,
                encryptionKeyBase64 = signedOutput.encryptionKeyBase64,
                signatureBase64 = signedOutput.signatureBase64,
                signatureAlgorithm = signedOutput.signatureAlgorithm,
            )

            val resultTokenOutput = processor.requestResultToken(
                context.getRequestParameters(
                    kid = encryptionParameters.jwk.kid,
                    scheme = encryptionParameters.schema.toString(),
                )
            )

            return transactionContext.copy(
                resultToken = resultTokenOutput.resultToken,
                resultTokenPayload = resultTokenOutput.resultTokenPayload,
            )
        } catch (e: DccTicketingException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e)
            // most likely null pointers from incomplete transaction context
            throw DccTicketingException(VS_ID_CLIENT_ERR)
        }
    }

    private fun DccTicketingTransactionContext.getEncryptionParameters(): EncryptionParameters {
        val schema: DccTicketingSecurityTool.Scheme
        val jwk: DccJWK
        if (!validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM.isNullOrEmpty()) {
            schema = DccTicketingSecurityTool.Scheme.RSAOAEPWithSHA256AESGCM
            jwk = validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM.first()
        } else if (!validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC.isNullOrEmpty()) {
            schema = DccTicketingSecurityTool.Scheme.RSAOAEPWithSHA256AESCBC
            jwk = validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC.first()
        } else throw DccTicketingException(VS_ID_NO_ENC_KEY)
        return EncryptionParameters(
            schema, jwk
        )
    }

    private fun DccTicketingTransactionContext.getSecurityToolInput(
        encryptionParameters: EncryptionParameters
    ): DccTicketingSecurityTool.Input {
        val publicKeyForEncryption = converter.createPublicKey(encryptionParameters.jwk)
        return DccTicketingSecurityTool.Input(
            dccBarcodeData = dccBarcodeData!!,
            nonceBase64 = nonceBase64!!,
            encryptionScheme = encryptionParameters.schema,
            publicKeyForEncryption = publicKeyForEncryption,
            privateKeyForSigning = ecPrivateKey!!
        )
    }

    private fun DccTicketingTransactionContext.getRequestParameters(
        kid: String,
        scheme: String
    ) = ResultTokenInput(
        serviceEndpoint = accessTokenPayload!!.aud,
        validationServiceJwkSet = validationServiceJwkSet!!,
        validationServiceSignKeyJwkSet = validationServiceSignKeyJwkSet!!,
        jwt = accessToken!!,
        encryptionKeyKid = kid,
        encryptedDCCBase64 = encryptedDCCBase64!!,
        encryptionKeyBase64 = encryptionKeyBase64!!,
        signatureBase64 = signatureBase64!!,
        signatureAlgorithm = signatureAlgorithm!!,
        encryptionScheme = scheme,
    )

    data class EncryptionParameters(
        val schema: DccTicketingSecurityTool.Scheme,
        val jwk: DccJWK
    )
}
