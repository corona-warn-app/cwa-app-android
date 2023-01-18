package de.rki.coronawarnapp.dccticketing.core.qrcode

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.bugreporting.censors.dccticketing.DccTicketingJwtCensor
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingInvalidQrCodeException.ErrorCode
import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

class DccTicketingQrCodeExtractor @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    private val jwtCensor: DccTicketingJwtCensor,
) : QrCodeExtractor<DccTicketingQrCode> {
    override suspend fun canHandle(rawString: String): Boolean {
        return rawString.startsWith(PREFIX)
    }

    override suspend fun extract(rawString: String): DccTicketingQrCode {
        return DccTicketingQrCode(
            qrCode = rawString,
            data = rawString.parse().validate().also {
                jwtCensor.addJwt(it.token)
            }
        )
    }

    private fun String.parse(): DccTicketingQrCodeData {
        return try {
            mapper.readValue(this)
        } catch (e: Exception) {
            throw DccTicketingInvalidQrCodeException(ErrorCode.INIT_DATA_PARSE_ERR)
        }
    }

    @Suppress("UselessCallOnNotNull")
    private fun DccTicketingQrCodeData.validate(): DccTicketingQrCodeData {
        if (protocol != PROTOCOL) throw DccTicketingInvalidQrCodeException(ErrorCode.INIT_DATA_PROTOCOL_INVALID)
        if (subject.isNullOrBlank()) throw DccTicketingInvalidQrCodeException(ErrorCode.INIT_DATA_SUBJECT_EMPTY)
        if (serviceProvider.isNullOrBlank()) throw DccTicketingInvalidQrCodeException(ErrorCode.INIT_DATA_SP_EMPTY)
        return this
    }
}

private const val PREFIX = "{"
private const val PROTOCOL = "DCCVALIDATION"
