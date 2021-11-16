package de.rki.coronawarnapp.dccticketing.core.transaction

data class DccTicketingAccessTokenService(
    val accessTokenService: DccTicketingService,
    val accessTokenServiceJwkSet: Set<DccJWK>,
    val accessTokenSignJwkSet: Set<DccJWK>,
)
