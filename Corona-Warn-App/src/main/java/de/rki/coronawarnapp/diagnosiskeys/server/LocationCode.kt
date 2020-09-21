package de.rki.coronawarnapp.diagnosiskeys.server

import java.util.Locale

data class LocationCode(
    private val rawIdentifier: String
) {
    @Transient
    val identifier: String = rawIdentifier.toUpperCase(Locale.ROOT)
}
