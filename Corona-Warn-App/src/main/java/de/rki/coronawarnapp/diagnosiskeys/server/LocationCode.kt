package de.rki.coronawarnapp.diagnosiskeys.server

data class LocationCode(
    private val rawIdentifier: String
) {
    val identifier: String
        get() = rawIdentifier.uppercase()
}
