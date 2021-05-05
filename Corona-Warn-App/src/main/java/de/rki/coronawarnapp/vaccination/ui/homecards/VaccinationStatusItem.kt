package de.rki.coronawarnapp.vaccination.ui.homecards

import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson

interface VaccinationStatusItem : HomeItem {
    val vaccinatedPerson: VaccinatedPerson

    override val stableId: Long
        get() = vaccinatedPerson.identifier.hashCode().toLong()
}
