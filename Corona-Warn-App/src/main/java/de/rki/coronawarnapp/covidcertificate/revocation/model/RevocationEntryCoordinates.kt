package de.rki.coronawarnapp.covidcertificate.revocation.model

// To Do: Implement
interface RevocationEntryCoordinates {

    enum class Type(type: String) {
        SIGNATURE("0a"),
        UCI("0b"),
        COUNTRYCODEUCI("0c")
    }
}
