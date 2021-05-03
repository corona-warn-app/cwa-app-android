package de.rki.coronawarnapp.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import org.joda.time.Instant

@Keep
data class ProofContainer(
    @SerializedName("proof") val proof: StoredProof,
    @SerializedName("expiresAt") val expiresAt: Instant,
    @SerializedName("updatedAt") val updatedAt: Instant,
    @SerializedName("proofCBORBase64") val proofCBORBase64: String,
) {
    fun toProofCertificate(): ProofCertificate = object : ProofCertificate {
        override val expiresAt: Instant
            get() = this@ProofContainer.expiresAt
        override val updatedAt: Instant
            get() = this@ProofContainer.updatedAt
    }

    data class StoredProof(
        @SerializedName("identifier") val identifier: String
    )
}
