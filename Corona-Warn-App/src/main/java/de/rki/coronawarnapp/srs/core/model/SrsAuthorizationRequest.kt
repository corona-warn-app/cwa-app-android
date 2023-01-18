package de.rki.coronawarnapp.srs.core.model

import com.google.protobuf.ByteString

data class SrsAuthorizationRequest(
    val srsOtp: SrsOtp,
    val androidId: ByteString,
    val safetyNetJws: String,
    val salt: String,
)
