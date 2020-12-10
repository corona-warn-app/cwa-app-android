package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

interface ContactDiaryPersonEncounter {
    val id: Long
    val date: LocalDate
    val contactDiaryPerson: ContactDiaryPerson
}
