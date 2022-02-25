package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant

@Deprecated("Used only for migration for legacy data")
data class VaccinatedPersonData(
    @SerializedName("vaccinationData")
    val vaccinations: Set<StoredVaccinationCertificateData> = emptySet(),

    @SerializedName("boosterRuleIdentifier")
    @Deprecated("the boosterRuleIdentifier is stored in DccWalletInfo from 2.18 onwards")
    val boosterRuleIdentifier: String? = null,

    @SerializedName("lastSeenBoosterRuleIdentifier")
    val lastSeenBoosterRuleIdentifier: String? = null,

    @SerializedName("lastBoosterNotifiedAt")
    val lastBoosterNotifiedAt: Instant? = null,
)
