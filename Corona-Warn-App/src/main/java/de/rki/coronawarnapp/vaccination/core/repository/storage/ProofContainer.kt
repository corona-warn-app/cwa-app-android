package de.rki.coronawarnapp.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import org.joda.time.Instant

data class ProofContainer(
    @SerializedName("expiresAt") override val expiresAt: Instant,
    @SerializedName("updatedAt") override val updatedAt: Instant,
) : ProofCertificate
