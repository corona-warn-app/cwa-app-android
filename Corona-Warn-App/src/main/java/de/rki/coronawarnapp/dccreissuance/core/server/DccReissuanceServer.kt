package de.rki.coronawarnapp.dccreissuance.core.server

import com.google.gson.Gson
import dagger.Lazy
import de.rki.coronawarnapp.dccreissuance.core.common.DccReissuanceException
import de.rki.coronawarnapp.dccreissuance.core.common.DccReissuanceException.ErrorCode
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceRequestBody
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceResponse
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class DccReissuanceServer @Inject constructor(
    private val dccReissuanceApiLazy: Lazy<DccReissuanceApi>,
    @BaseGson private val gson: Gson
) {

    private val dccReissuanceApi
        get() = dccReissuanceApiLazy.get()

    suspend fun requestDccReissuance(): List<DccReissuanceResponse> {
        val dccReissuanceRequestBody = DccReissuanceRequestBody("combine", listOf("cert"))
        return dccReissuanceApi.requestReissuance(dccReissuanceRequestBody = dccReissuanceRequestBody)
            .also { it.throwIfFailed() }
            .body()
            .parse()
    }

    private fun Response<*>.throwIfFailed() {
        Timber.tag(TAG).d("Checking if response=%s failed", this)

        if (isSuccessful) {
            Timber.d("Response is successful")
            return
        }

        when (code()) {
            400 -> ErrorCode.DCC_RI_400
            401 -> ErrorCode.DCC_RI_401
            403 -> ErrorCode.DCC_RI_403
            406 -> ErrorCode.DCC_RI_406
            500 -> ErrorCode.DCC_RI_500
            in 400..499 -> ErrorCode.DCC_RI_CLIENT_ERR
            else -> ErrorCode.DCC_RI_SERVER_ERR
        }.also { throw DccReissuanceException(errorCode = it) }
    }

    private fun ResponseBody?.parse(): List<DccReissuanceResponse> = try {
        this!!.charStream().use { gson.fromJson(it) }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse %s", this)
        throw DccReissuanceException(errorCode = ErrorCode.DCC_RI_PARSE_ERR, cause = e)
    }

    companion object {
        private val TAG = tag<DccReissuanceServer>()
    }
}
