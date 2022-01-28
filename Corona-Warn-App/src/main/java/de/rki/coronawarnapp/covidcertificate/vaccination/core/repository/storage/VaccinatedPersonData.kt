package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import org.joda.time.Instant

data class VaccinatedPersonData(
    @SerializedName("vaccinationData")
    val vaccinations: Set<VaccinationContainer> = emptySet(),

    @Transient val boosterRule: DccValidationRule? = null,
    @SerializedName("boosterRuleIdentifier")
    @Deprecated("the boosterRuleIdentifier is stored in DccWalletInfo from 2.18 onwards")
    val boosterRuleIdentifier: String? = null,

    @SerializedName("lastSeenBoosterRuleIdentifier")
    val lastSeenBoosterRuleIdentifier: String? = null,

    @SerializedName("lastBoosterNotifiedAt")
    val lastBoosterNotifiedAt: Instant? = null,
) {
    val identifier: CertificatePersonIdentifier
        get() = vaccinations.first().personIdentifier
}
