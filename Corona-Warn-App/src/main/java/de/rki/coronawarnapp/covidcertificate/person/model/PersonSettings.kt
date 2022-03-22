package de.rki.coronawarnapp.covidcertificate.person.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.Instant

data class PersonSettings(
    @JsonProperty("lastSeenBoosterRuleIdentifier")
    val lastSeenBoosterRuleIdentifier: String? = null,

    @JsonProperty("lastBoosterNotifiedAt")
    val lastBoosterNotifiedAt: Instant? = null,

    @JsonProperty("showDccReissuanceBadge")
    val showDccReissuanceBadge: Boolean = false,

    @JsonProperty("lastDccReissuanceNotifiedAt")
    val lastDccReissuanceNotifiedAt: Instant? = null,

    @JsonProperty("showAdmissionStateChangedBadge")
    val showAdmissionStateChangedBadge: Boolean = false,

    @JsonProperty("lastAdmissionStateNotifiedAt")
    val lastAdmissionStateNotifiedAt: Instant? = null,
)
