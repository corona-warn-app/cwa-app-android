package de.rki.coronawarnapp.srs.core.model

import okio.ByteString

data class SrsAuthorizationRequest(
    val srsOtp: SrsOtp,
    val androidId: ByteString,
    val safetyNetJws: String,
    val salt: String,
)
