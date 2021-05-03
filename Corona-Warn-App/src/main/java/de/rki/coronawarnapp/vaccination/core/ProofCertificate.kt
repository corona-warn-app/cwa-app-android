package de.rki.coronawarnapp.vaccination.core

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

@Keep
data class ProofCertificate(
    @SerializedName("expiresAt")
    val expiresAt: Instant
)
