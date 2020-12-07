package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

interface ContactDiaryElement {

    val createdAt: Instant

    val people: List<Person>
    val locations: List<Location>

    val numberOfPersons: Int?
        get() = people?.size

    val numberOfLocations: Int?
        get() = locations?.size
}
