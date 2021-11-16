package de.rki.coronawarnapp.dccticketing.core.service.processor

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import javax.inject.Inject

@Reusable
class AccessTokenRequestProcessor @Inject constructor() {

    suspend fun requestAccessToken(): Output {
        TODO("Add input and implement")
    }

    data class Output(
        val accessToken: String,
        val accessTokenPayload: DccTicketingAccessToken,
        val nonceBase64: String
    )
}
