package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

data class ContactDiaryEntry(
    val person: List<Person>?,
    val location: List<Location>?,
    val dateAddedEntry: Instant?
)
