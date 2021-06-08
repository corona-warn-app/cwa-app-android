package de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards

import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson

interface VaccinationStatusItem : CertificatesItem {
    val vaccinatedPerson: VaccinatedPerson

    override val stableId: Long
        get() = vaccinatedPerson.identifier.hashCode().toLong()
}
