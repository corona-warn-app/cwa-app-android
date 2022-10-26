package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@Deprecated("Used only for migration of legacy data")
data class VaccinatedPersonData(
    @JsonProperty("vaccinationData")
    val vaccinations: Set<StoredVaccinationCertificateData> = emptySet(),

    @JsonProperty("boosterRuleIdentifier")
    @Deprecated("the boosterRuleIdentifier is stored in DccWalletInfo from 2.18 onwards")
    val boosterRuleIdentifier: String? = null,

    @JsonProperty("lastSeenBoosterRuleIdentifier")
    val lastSeenBoosterRuleIdentifier: String? = null,

    @JsonProperty("lastBoosterNotifiedAt")
    val lastBoosterNotifiedAt: Instant? = null,
)
