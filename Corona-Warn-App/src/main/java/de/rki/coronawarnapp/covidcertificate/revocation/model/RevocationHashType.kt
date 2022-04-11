package de.rki.coronawarnapp.covidcertificate.revocation.model

/** A hex-encoded byte representing the hash type (2 characters)  */
enum class RevocationHashType(val type: String) {
    SIGNATURE("0a"),
    UCI("0b"),
    COUNTRYCODEUCI("0c")
}
