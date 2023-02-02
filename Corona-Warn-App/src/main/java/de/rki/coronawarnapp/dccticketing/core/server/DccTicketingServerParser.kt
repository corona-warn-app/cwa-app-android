package de.rki.coronawarnapp.dccticketing.core.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.tag
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingServerParser @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper
) {

    @Throws(DccTicketingServerException::class)
    fun createServiceIdentityDocument(response: Response<ResponseBody>): DccTicketingServiceIdentityDocument =
        response.parse()

    private inline fun <reified T> Response<ResponseBody>.parse(): T = try {
        Timber.tag(TAG).d("Parsing response=%s", this)
        body()!!.charStream().use { mapper.readValue(it) }
    } catch (e: Exception) {
        Timber.e(e, "Parsing failed")
        throw DccTicketingServerException(errorCode = DccTicketingServerException.ErrorCode.PARSE_ERR, cause = e)
    }

    companion object {
        private val TAG = tag<DccTicketingServerParser>()
    }
}
