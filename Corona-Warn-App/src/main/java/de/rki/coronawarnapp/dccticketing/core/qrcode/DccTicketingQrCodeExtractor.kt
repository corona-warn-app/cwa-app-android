package de.rki.coronawarnapp.dccticketing.core.qrcode

import com.google.gson.Gson
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingException.ErrorCode
import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import javax.inject.Inject

class DccTicketingQrCodeExtractor @Inject constructor(
    @BaseGson private val gson: Gson
) : QrCodeExtractor<DccTicketingQrCode> {
    override suspend fun canHandle(rawString: String): Boolean {
        return try {
            rawString.startsWith(PREFIX) && gson.fromJson<DccTicketingData>(rawString).protocol == PROTOCOL
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun extract(rawString: String): DccTicketingQrCode {
        return DccTicketingQrCode(
            qrCode = rawString,
            data = rawString.parse().validate()
        )
    }

    private fun String.parse(): DccTicketingData {
        return try {
            gson.fromJson(this)
        } catch (e: Exception) {
            throw DccTicketingException(ErrorCode.INIT_DATA_PARSE_ERR)
        }
    }

    private fun DccTicketingData.validate(): DccTicketingData {
        if (protocol != PROTOCOL) throw DccTicketingException(ErrorCode.INIT_DATA_PROTOCOL_INVALID)
        if (subject.isNullOrBlank()) throw DccTicketingException(ErrorCode.INIT_DATA_SUBJECT_EMPTY)
        if (serviceProvider.isNullOrBlank()) throw DccTicketingException(ErrorCode.INIT_DATA_SP_EMPTY)
        return this
    }
}

private const val PREFIX = "{"
private const val PROTOCOL = "DCCVALIDATION"
