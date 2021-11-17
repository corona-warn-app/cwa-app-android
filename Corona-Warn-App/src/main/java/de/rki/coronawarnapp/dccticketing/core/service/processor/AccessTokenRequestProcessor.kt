package de.rki.coronawarnapp.dccticketing.core.service.processor

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import javax.inject.Inject

@Reusable
class AccessTokenRequestProcessor @Inject constructor() {

    suspend fun requestAccessToken(): Output {
        val accessToken = ""
        val accessTokenPayload = DccTicketingAccessToken("", 0, 0, "", "", "", "", 0, null)
        val nonceBase64 = ""
        // TODO: Add input and replace dummy impl
        return Output(
            accessToken = accessToken,
            accessTokenPayload = accessTokenPayload,
            nonceBase64 = nonceBase64
        )
    }

    data class Output(
        val accessToken: String,
        val accessTokenPayload: DccTicketingAccessToken,
        val nonceBase64: String
    )
}
