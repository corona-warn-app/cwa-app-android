package de.rki.coronawarnapp.srs.core.model

data class SrsAuthorizationFakeRequest(
    val safetyNetJws: String,
    val salt: String,
)
