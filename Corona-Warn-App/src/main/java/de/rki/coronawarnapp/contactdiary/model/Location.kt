package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

interface Location {
    val createdAt: Instant
    val location: Location
}
