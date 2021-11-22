package de.rki.coronawarnapp.dccticketing.core.service.processor

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateChecker
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.JwtTokenConverter
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.ResultTokenRequest
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.tag
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ResultTokenRequestProcessor @Inject constructor(
    private val dccTicketingServer: DccTicketingServer,
    private val dccTicketingServerCertificateChecker: DccTicketingServerCertificateChecker,
    private val convertor: JwtTokenConverter,
) {

    suspend fun requestResultToken(resultTokenInput: ResultTokenInput): ResultTokenOutput {
        // 1. Call Validation Service
        val response = resultTokenResponse(resultTokenInput)
        // Checking the Server Certificate Against a Set of JWKs.
        checkServerCertificate(response, resultTokenInput.validationServiceJwkSet)
        // 2. Find `resultToken`
        val resultToken = response.body() ?: throw DccTicketingException(DccTicketingErrorCode.RTR_SERVER_ERR)
        // 3. Verify signature the signature of the resultToken
        verifyJWT(resultToken, resultTokenInput.validationServiceSignKeyJwkSet)

        // 4.Determine resultTokenPayload: the resultTokenPayload
        return ResultTokenOutput(
            resultToken = resultToken,
            resultTokenPayload = convertor.jsonToResultToken(resultToken)
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun checkServerCertificate(
        response: Response<String>,
        jwkSet: Set<DccJWK>
    ) = try {
        dccTicketingServerCertificateChecker.checkCertificate(
            response.raw().handshake?.peerCertificates.orEmpty(),
            jwkSet
        )
    } catch (e: DccTicketingServerCertificateCheckException) {
        Timber.tag(TAG).e(e, "checkServerCertificate for result token failed")
        when (e.errorCode) {
            DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID ->
                DccTicketingException.ErrorCode.RTR_CERT_PIN_NO_JWK_FOR_KID
            DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH ->
                DccTicketingException.ErrorCode.RTR_CERT_PIN_MISMATCH
        }.let { DccTicketingException(it) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun verifyJWT(jwt: String, jwkSet: Set<DccJWK>) = try {
        // TODO
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "verifyJWT for result token failed")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun resultTokenResponse(resultTokenInput: ResultTokenInput) =
        try {
            dccTicketingServer.getResultToken(
                url = resultTokenInput.serviceEndpoint,
                authorizationHeader = "Bearer ${resultTokenInput.jwt}",
                requestBody = resultTokenInput.run {
                    ResultTokenRequest(
                        kid = encryptionKeyKid,
                        dcc = encryptedDCCBase64,
                        sig = signatureBase64,
                        encKey = encryptionKeyBase64,
                        encScheme = encryptionScheme,
                        sigAlg = signatureAlgorithm
                    )
                }
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Requesting result token failed")
            throw when (e) {
                is CwaUnknownHostException,
                is NetworkReadTimeoutException,
                is NetworkConnectTimeoutException -> DccTicketingErrorCode.RTR_NO_NETWORK
                is CwaClientError -> DccTicketingErrorCode.RTR_CLIENT_ERR
                // Blame the server for everything else
                else -> DccTicketingErrorCode.RTR_SERVER_ERR
            }.let { DccTicketingException(it, e) }
        }

    companion object {
        private val TAG = tag<ResultTokenRequestProcessor>()
    }
}

data class ResultTokenInput(
    val serviceEndpoint: String,
    val validationServiceJwkSet: Set<DccJWK>,
    val validationServiceSignKeyJwkSet: Set<DccJWK>,
    val jwt: String,
    val encryptionKeyKid: String,
    val encryptedDCCBase64: String,
    val encryptionKeyBase64: String,
    val signatureBase64: String,
    val signatureAlgorithm: String,
    val encryptionScheme: String,
)

@Parcelize
data class ResultTokenOutput(
    val resultToken: String,
    val resultTokenPayload: DccTicketingResultToken
) : Parcelable
