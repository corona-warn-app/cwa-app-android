package de.rki.coronawarnapp.dccticketing.core.transaction

data class DccTicketingValidationService(
    val validationService: DccTicketingService,
    val validationServiceJwkSet: Set<DccJWK>,
)
