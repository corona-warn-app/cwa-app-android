package de.rki.coronawarnapp.dccticketing.core.service.processor

import javax.inject.Inject

class AccessTokenRequestProcessor @Inject constructor() {

    suspend fun requestAccessToken(): Output {
        TODO("Add input and implement")
    }

    data class Output(
        val accessToken: String,
        val accessTokenPayload: Any, // Change to DccTicketingAccessToken, from PR EXPOSUREAPP-10484
        val nonceBase64: String
    )
}
