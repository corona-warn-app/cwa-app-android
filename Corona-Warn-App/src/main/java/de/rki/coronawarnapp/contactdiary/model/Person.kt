package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

interface Person {
    val createdAt: Instant
    val person: Person
}
