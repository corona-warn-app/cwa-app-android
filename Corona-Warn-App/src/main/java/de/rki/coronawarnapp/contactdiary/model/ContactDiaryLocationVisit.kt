package de.rki.coronawarnapp.contactdiary.model

import java.time.Duration
import java.time.LocalDate

interface ContactDiaryLocationVisit {
    val id: Long
    val date: LocalDate
    val contactDiaryLocation: ContactDiaryLocation

    /*
        Value in miliseconds
     */
    val duration: Duration?
    val circumstances: String?
    val checkInID: Long?
}

fun List<ContactDiaryLocationVisit>.sortByNameAndIdASC(): List<ContactDiaryLocationVisit> =
    this.sortedWith(
        compareBy(
            { it.contactDiaryLocation.locationName.lowercase() },
            { it.contactDiaryLocation.locationId }
        )
    )
