package de.rki.coronawarnapp.vaccination.ui.homecard

import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson

interface VaccinationStatusItem : CertificatesItem {
    val vaccinatedPerson: VaccinatedPerson

    override val stableId: Long
        get() = vaccinatedPerson.identifier.hashCode().toLong()
}
