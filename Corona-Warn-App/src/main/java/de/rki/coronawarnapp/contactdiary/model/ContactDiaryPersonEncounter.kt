package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate
import java.util.Locale

interface ContactDiaryPersonEncounter {
    val id: Long
    val date: LocalDate
    val contactDiaryPerson: ContactDiaryPerson
}

fun List<ContactDiaryPersonEncounter>.sortByNameAndIdASC(): List<ContactDiaryPersonEncounter> =
    this.sortedWith(
        compareBy(
            { it.contactDiaryPerson.fullName.toLowerCase(Locale.ROOT) },
            { it.contactDiaryPerson.personId }
        )
    )
