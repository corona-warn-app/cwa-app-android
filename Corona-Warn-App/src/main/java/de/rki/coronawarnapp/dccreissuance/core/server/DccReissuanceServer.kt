package de.rki.coronawarnapp.dccreissuance.core.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.contains
import com.google.gson.Gson
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException.ErrorCode
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceRequestBody
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.dccreissuance.core.server.validation.DccReissuanceServerCertificateValidator
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.http.serverCertificateChain
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@Reusable
class DccReissuanceServer @Inject constructor(
    private val dccReissuanceApiLazy: Lazy<DccReissuanceApi>,
    private val dispatcherProvider: DispatcherProvider,
    private val dccReissuanceServerCertificateValidator: DccReissuanceServerCertificateValidator,
    @BaseGson private val gson: Gson,
    @BaseJackson private val objectMapper: ObjectMapper
) {

    private val dccReissuanceApi
        get() = dccReissuanceApiLazy.get()

    suspend fun requestDccReissuance(
        action: String,
        certificates: List<String>
    ): DccReissuanceResponse = withContext(dispatcherProvider.IO) {
        try {
            val dccReissuanceRequestBody = DccReissuanceRequestBody(action = action, certificates = certificates)
            dccReissuanceApi.requestReissuance(dccReissuanceRequestBody = dccReissuanceRequestBody)
                .parseAndValidate()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to request Dcc Reissuance")
            throw when (e) {
                is DccReissuanceException -> throw e
                is UnknownHostException,
                is SocketTimeoutException,
                is NetworkReadTimeoutException -> ErrorCode.DCC_RI_NO_NETWORK
                else -> ErrorCode.DCC_RI_SERVER_ERR
            }.let { DccReissuanceException(errorCode = it, cause = e) }
        }
    }

    private fun Response<ResponseBody>.throwIfFailed() {
        Timber.tag(TAG).d("Check if response=%s failed", this)

        if (isSuccessful) {
            Timber.d("Response is successful")
            return
        }

        val serverErrorCode = errorBody()?.charStream().use {
            val errorResponse = objectMapper.readTree(it)
            Timber.tag(TAG).w("Server errorResponse=%s", errorResponse)
            when (errorResponse.contains(SERVER_ERROR_CODE)) {
                true -> errorResponse[SERVER_ERROR_CODE].asText()
                false -> null
            }
        }

        when (code()) {
            400 -> ErrorCode.DCC_RI_400
            401 -> ErrorCode.DCC_RI_401
            403 -> ErrorCode.DCC_RI_403
            406 -> ErrorCode.DCC_RI_406
            429 -> ErrorCode.DCC_RI_429
            500 -> ErrorCode.DCC_RI_500
            in 400..499 -> ErrorCode.DCC_RI_CLIENT_ERR
            else -> ErrorCode.DCC_RI_SERVER_ERR
        }.also { throw DccReissuanceException(errorCode = it, serverErrorCode = serverErrorCode) }
    }

    private suspend fun Response<ResponseBody>.parseAndValidate(): DccReissuanceResponse {
        Timber.tag(TAG).d("Parse and validate response=%s", this)
        throwIfFailed()

        val serverCertificateChain = raw().serverCertificateChain
        dccReissuanceServerCertificateValidator.checkCertificateChain(certificateChain = serverCertificateChain)

        return try {
            val body = checkNotNull(body()) { "Response body was null" }
            val dccReissuances: List<DccReissuanceResponse.DccReissuance> = body.charStream().use { gson.fromJson(it) }
            DccReissuanceResponse(dccReissuances = dccReissuances)
        } catch (e: Exception) {
            throw DccReissuanceException(errorCode = ErrorCode.DCC_RI_PARSE_ERR, cause = e)
        }
    }

    companion object {
        private val TAG = tag<DccReissuanceServer>()
        private const val SERVER_ERROR_CODE = "errorCode"
    }
}
