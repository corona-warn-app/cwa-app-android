package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

interface ContactDiaryElement {

    val createdAt: Instant

    val defaultPeople: List<DefaultPerson>
    val defaultLocations: List<DefaultLocation>

    val numberOfPersons: Int?
        get() = defaultPeople?.size

    val numberOfLocations: Int?
        get() = defaultLocations?.size
}
