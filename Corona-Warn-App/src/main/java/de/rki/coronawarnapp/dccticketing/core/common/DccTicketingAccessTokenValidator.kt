package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import timber.log.Timber

fun DccTicketingAccessToken.validate() {
    if (t !in 1..2) {
        Timber.w("DccTicketingAccessToken.t = $t is not a valid type")
        throw DccTicketingException(DccTicketingException.ErrorCode.ATR_PARSE_ERR)
    }
    if (aud.isBlank()) {
        Timber.w("DccTicketingAccessToken.aud shouldn't be empty")
        throw DccTicketingException(DccTicketingException.ErrorCode.ATR_AUD_INVALID)
    }
}
