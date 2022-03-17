package de.rki.coronawarnapp.dccreissuance.core.server

import com.google.gson.Gson
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException.ErrorCode
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceErrorResponse
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceRequestBody
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.dccreissuance.core.server.validation.DccReissuanceServerCertificateValidator
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.http.serverCertificateChain
import de.rki.coronawarnapp.util.serialization.BaseGson
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
    @BaseGson private val gson: Gson
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

        val serverError = tryGetServerError()

        throw when (code()) {
            400 -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_400, serverErrorResponse = serverError)
            401 -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_401, serverErrorResponse = serverError)
            403 -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_403, serverErrorResponse = serverError)
            406 -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_406, serverErrorResponse = serverError)
            429 -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_429, serverErrorResponse = serverError)
            500 -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_500, serverErrorResponse = serverError)
            in 400..499 -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_CLIENT_ERR)
            else -> DccReissuanceException(errorCode = ErrorCode.DCC_RI_SERVER_ERR)
        }
    }

    private fun Response<ResponseBody>.tryGetServerError(): DccReissuanceErrorResponse? = try {
        errorBody()?.charStream()?.use { gson.fromJson(it) }
    } catch (e: Exception) {
        null
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
    }
}
