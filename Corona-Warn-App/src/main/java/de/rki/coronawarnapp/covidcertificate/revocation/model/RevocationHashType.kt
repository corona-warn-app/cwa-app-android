package de.rki.coronawarnapp.covidcertificate.revocation.model

import com.fasterxml.jackson.annotation.JsonProperty
import okio.ByteString

/** A hex-encoded byte representing the hash type (2 characters)  */
enum class RevocationHashType(val type: String) {
    @JsonProperty("SIGNATURE")
    SIGNATURE("0a"),

    @JsonProperty("UCI")
    UCI("0b"),

    @JsonProperty("COUNTRYCODEUCI")
    COUNTRYCODEUCI("0c");

    companion object {
        fun from(byteString: ByteString): RevocationHashType {
            val hex = byteString.hex()
            val type = values().firstOrNull { it.type == hex }
            return checkNotNull(type) { "Found no hash type for $hex" }
        }
    }
}
