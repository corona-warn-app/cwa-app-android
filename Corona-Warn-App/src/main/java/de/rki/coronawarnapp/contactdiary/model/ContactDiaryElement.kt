package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

interface ContactDiaryElement {

    val date: LocalDate

    val contactDiaryPeople: MutableList<out ContactDiaryPerson>
    val contactDiaryLocations: MutableList<out ContactDiaryLocation>

    val numberOfPersons: Int
        get() = contactDiaryPeople.size

    val numberOfLocations: Int
        get() = contactDiaryLocations.size
}
