package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import org.joda.time.Instant

// TODO: remove
data class VaccinatedPersonData(
    @SerializedName("vaccinationData")
    val vaccinations: Set<VaccinationContainer> = emptySet(),

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
