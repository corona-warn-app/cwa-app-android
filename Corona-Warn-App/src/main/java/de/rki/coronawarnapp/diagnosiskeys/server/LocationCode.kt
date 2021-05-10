package de.rki.coronawarnapp.diagnosiskeys.server

import java.util.Locale

data class LocationCode(
    private val rawIdentifier: String
) {
    val identifier: String
        get() = rawIdentifier.lowercase()
}
