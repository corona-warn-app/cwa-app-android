package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken

fun DccTicketingAccessToken.validate() {
    if (t !in 1..2) throw DccTicketingException(DccTicketingException.ErrorCode.ATR_PARSE_ERR)
    if (aud.isBlank()) throw DccTicketingException(DccTicketingException.ErrorCode.ATR_AUD_INVALID)
}
