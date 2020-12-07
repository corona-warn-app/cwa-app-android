package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

data class DefaultContactDiaryElement(
    val defaultPeople: List<DefaultPerson>,
    val defaultLocation: List<DefaultLocation>,
    val dateAddedEntry: Instant?
)
