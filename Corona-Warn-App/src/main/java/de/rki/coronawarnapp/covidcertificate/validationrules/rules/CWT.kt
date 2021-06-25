package de.rki.coronawarnapp.covidcertificate.validationrules.rules

enum class CWT(val value: String) {
    ISS("iss"),
    IAT("iat"),
    EXP("exp")
}
