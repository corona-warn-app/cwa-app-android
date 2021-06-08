package de.rki.coronawarnapp.vaccination.ui.cards

import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson

interface VaccinationStatusItem : CertificatesItem {
    val vaccinatedPerson: VaccinatedPerson

    override val stableId: Long
        get() = vaccinatedPerson.identifier.hashCode().toLong()
}
