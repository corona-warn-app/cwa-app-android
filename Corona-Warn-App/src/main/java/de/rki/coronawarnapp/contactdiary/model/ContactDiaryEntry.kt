package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

data class ContactDiaryEntry(
    val person: Person,
    val location: Location,
    val dateAddedEntry: Instant?
)
