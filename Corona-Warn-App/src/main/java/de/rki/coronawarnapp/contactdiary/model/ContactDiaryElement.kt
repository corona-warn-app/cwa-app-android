package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

interface ContactDiaryElement {

    val date: LocalDate

    val people: MutableList<out Person>
    val locations: MutableList<out Location>

    val numberOfPersons: Int
        get() = people.size

    val numberOfLocations: Int
        get() = locations.size
}
