package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

interface ContactDiaryPersonEncounter {
    val id: Long
    val date: LocalDate
    val contactDiaryPerson: ContactDiaryPerson
}

fun List<ContactDiaryPersonEncounter>.sortByNameAndIdASC(): List<ContactDiaryPersonEncounter> =
    this.sortedWith(compareBy({ it.contactDiaryPerson.fullName }, { it.contactDiaryPerson.personId }))
