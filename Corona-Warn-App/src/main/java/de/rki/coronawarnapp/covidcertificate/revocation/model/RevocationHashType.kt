package de.rki.coronawarnapp.covidcertificate.revocation.model

import okio.ByteString

/** A hex-encoded byte representing the hash type (2 characters)  */
enum class RevocationHashType(val type: String) {
    SIGNATURE("0a"),
    UCI("0b"),
    COUNTRYCODEUCI("0c");

    companion object {
        fun from(byteString: ByteString): RevocationHashType {
            val hex = byteString.hex()
            val type = values().firstOrNull { it.type == hex }
            return checkNotNull(type) { "Found no hash type for $hex" }
        }
    }
}
