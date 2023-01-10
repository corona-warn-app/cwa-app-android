package de.rki.coronawarnapp.coronatest.server

import com.fasterxml.jackson.annotation.JsonProperty

enum class VerificationKeyType {
    @JsonProperty("GUID")
    GUID,

    @JsonProperty("TELETAN")
    TELETAN;
}
