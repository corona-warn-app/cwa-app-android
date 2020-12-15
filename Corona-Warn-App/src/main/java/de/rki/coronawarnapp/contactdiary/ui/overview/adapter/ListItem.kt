package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import org.joda.time.LocalDate

data class ListItem(
    val date: LocalDate
) {
    val locations: MutableList<ContactDiaryLocation> = mutableListOf()
    val persons: MutableList<ContactDiaryPerson> = mutableListOf()
}
